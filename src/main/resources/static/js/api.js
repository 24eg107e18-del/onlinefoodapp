/**
 * Online Food Ordering System - Central API Client
 */

const API_BASE = '/api';

const TOKEN_KEY = 'food_ordering_jwt';
const USER_KEY = 'food_ordering_user';

export const authStorage = {
    getToken() {
        return localStorage.getItem(TOKEN_KEY);
    },
    setToken(token) {
        localStorage.setItem(TOKEN_KEY, token);
    },
    removeToken() {
        localStorage.removeItem(TOKEN_KEY);
    },
    getUser() {
        try {
            const raw = localStorage.getItem(USER_KEY);
            return raw ? JSON.parse(raw) : null;
        } catch (e) {
            return null;
        }
    },
    setUser(user) {
        localStorage.setItem(USER_KEY, JSON.stringify(user));
    },
    removeUser() {
        localStorage.removeItem(USER_KEY);
    },
    isAuthenticated() {
        return !!this.getToken();
    },
    isAdmin() {
        const user = this.getUser();
        return user && user.role === 'ADMIN';
    },
    logout() {
        this.removeToken();
        this.removeUser();
        window.location.href = '/login.html';
    }
};

async function request(endpoint, options = {}) {
    const url = `${API_BASE}${endpoint}`;
    const headers = {
        'Content-Type': 'application/json',
        ...(options.headers || {})
    };

    const token = authStorage.getToken();
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const config = {
        ...options,
        headers
    };

    try {
        const response = await fetch(url, config);

        if (response.status === 204) {
            return null;
        }

        const data = await response.json().catch(() => null);

        if (!response.ok) {
            // Handle session expiration
            if (response.status === 401 && !url.includes('/auth/login')) {
                authStorage.logout();
            }
            const errorMessage = data?.message || (data?.fieldErrors ? Object.values(data.fieldErrors).join(', ') : 'Request failed');
            const error = new Error(errorMessage);
            error.status = response.status;
            error.data = data;
            throw error;
        }

        return data;
    } catch (err) {
        throw err;
    }
}

export const api = {
    // Auth endpoints
    auth: {
        register(userData) {
            return request('/auth/register', {
                method: 'POST',
                body: JSON.stringify(userData)
            });
        },
        login(credentials) {
            return request('/auth/login', {
                method: 'POST',
                body: JSON.stringify(credentials)
            });
        }
    },

    // Food Items
    foods: {
        getAll(params = {}) {
            const query = new URLSearchParams();
            if (params.search) query.append('search', params.search);
            if (params.category) query.append('category', params.category);
            if (params.available !== undefined) query.append('available', params.available);
            if (params.page !== undefined) query.append('page', params.page);
            if (params.size !== undefined) query.append('size', params.size);

            const qs = query.toString();
            return request(`/foods${qs ? `?${qs}` : ''}`);
        },
        getById(id) {
            return request(`/foods/${id}`);
        },
        create(data) {
            return request('/foods', {
                method: 'POST',
                body: JSON.stringify(data)
            });
        },
        update(id, data) {
            return request(`/foods/${id}`, {
                method: 'PUT',
                body: JSON.stringify(data)
            });
        },
        delete(id) {
            return request(`/foods/${id}`, {
                method: 'DELETE'
            });
        }
    },

    // Categories
    categories: {
        getAll(activeOnly = false) {
            return request(`/categories${activeOnly ? '?activeOnly=true' : ''}`);
        },
        getById(id) {
            return request(`/categories/${id}`);
        },
        create(data) {
            return request('/categories', {
                method: 'POST',
                body: JSON.stringify(data)
            });
        },
        update(id, data) {
            return request(`/categories/${id}`, {
                method: 'PUT',
                body: JSON.stringify(data)
            });
        },
        delete(id) {
            return request(`/categories/${id}`, {
                method: 'DELETE'
            });
        }
    },

    // Cart
    cart: {
        get() {
            return request('/cart');
        },
        add(foodItemId, quantity = 1) {
            return request('/cart/add', {
                method: 'POST',
                body: JSON.stringify({ foodItemId, quantity })
            });
        },
        update(foodItemId, quantity) {
            return request(`/cart/${foodItemId}`, {
                method: 'PUT',
                body: JSON.stringify({ quantity })
            });
        },
        remove(foodItemId) {
            return request(`/cart/${foodItemId}`, {
                method: 'DELETE'
            });
        },
        clear() {
            return request('/cart/clear', {
                method: 'DELETE'
            });
        }
    },

    // Orders
    orders: {
        place(deliveryAddress, paymentMethod = 'CASH_ON_DELIVERY', transactionId = null) {
            return request('/orders', {
                method: 'POST',
                body: JSON.stringify({ deliveryAddress, paymentMethod, transactionId })
            });
        },
        getMyOrders() {
            return request('/orders');
        },
        getById(id) {
            return request(`/orders/${id}`);
        },
        updateStatus(id, status) {
            return request(`/orders/${id}/status`, {
                method: 'PUT',
                body: JSON.stringify({ status })
            });
        }
    },

    // User Profile
    users: {
        getProfile() {
            return request('/users/profile');
        },
        updateProfile(data) {
            return request('/users/profile', {
                method: 'PUT',
                body: JSON.stringify(data)
            });
        }
    },

    // Admin Dashboard & Operations
    admin: {
        getDashboard() {
            return request('/admin/dashboard');
        },
        getOrders(page, size = 15) {
            const query = new URLSearchParams();
            if (page !== undefined) query.append('page', page);
            if (size !== undefined) query.append('size', size);
            const qs = query.toString();
            return request(`/admin/orders${qs ? `?${qs}` : ''}`);
        },
        getUsers(page, size = 15) {
            const query = new URLSearchParams();
            if (page !== undefined) query.append('page', page);
            if (size !== undefined) query.append('size', size);
            const qs = query.toString();
            return request(`/admin/users${qs ? `?${qs}` : ''}`);
        }
    }
};
