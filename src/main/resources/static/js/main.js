/**
 * Online Food Ordering System - Global UI Helpers & Navigation Controller
 */

import { api, authStorage } from './api.js';

export function formatCurrency(amount) {
    if (amount === null || amount === undefined) return '$0.00';
    return `$${Number(amount).toFixed(2)}`;
}

export function formatDate(isoString) {
    if (!isoString) return '';
    const date = new Date(isoString);
    return date.toLocaleString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

export function getFallbackImage(category = 'food') {
    return 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=600&auto=format&fit=crop&q=80';
}

export function showToast(message, type = 'info') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    let icon = 'ℹ️';
    if (type === 'success') icon = '✅';
    if (type === 'error') icon = '⚠️';

    toast.innerHTML = `<span>${icon}</span> <span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transition = 'opacity 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

export async function refreshCartCount() {
    const badge = document.getElementById('nav-cart-count');
    if (!badge) return;

    if (!authStorage.isAuthenticated()) {
        badge.textContent = '0';
        return;
    }

    try {
        const cart = await api.cart.get();
        badge.textContent = cart?.totalQuantity || '0';
    } catch (e) {
        // If not logged in or error, reset to 0
        badge.textContent = '0';
    }
}

export function renderNavigation() {
    const navActions = document.getElementById('nav-user-actions');
    const navLinks = document.getElementById('nav-links');
    if (!navActions) return;

    const user = authStorage.getUser();
    const isAuth = authStorage.isAuthenticated();
    const isAdmin = authStorage.isAdmin();

    // Setup Nav Links
    if (navLinks) {
        let linksHtml = `
            <li><a href="/index.html" class="nav-link">Home</a></li>
            <li><a href="/menu.html" class="nav-link">Menu</a></li>
        `;
        if (isAuth && !isAdmin) {
            linksHtml += `
                <li><a href="/my-orders.html" class="nav-link">My Orders</a></li>
                <li><a href="/profile.html" class="nav-link">Profile</a></li>
            `;
        }
        if (isAdmin) {
            linksHtml += `
                <li><a href="/admin-dashboard.html" class="nav-link">Admin Dashboard</a></li>
                <li><a href="/manage-food.html" class="nav-link">Manage Food</a></li>
                <li><a href="/manage-categories.html" class="nav-link">Categories</a></li>
                <li><a href="/manage-orders.html" class="nav-link">Orders</a></li>
                <li><a href="/manage-customers.html" class="nav-link">Customers</a></li>
            `;
        }
        navLinks.innerHTML = linksHtml;

        // Highlight active link
        const currentPath = window.location.pathname;
        const allNavLinks = navLinks.querySelectorAll('.nav-link');
        allNavLinks.forEach(link => {
            if (link.getAttribute('href') === currentPath ||
                (currentPath === '/' && link.getAttribute('href') === '/index.html')) {
                link.classList.add('active');
            }
        });
    }

    // Setup User Actions
    if (!isAuth) {
        navActions.innerHTML = `
            <a href="/cart.html" class="cart-btn" aria-label="Cart">
                🛒 Cart <span class="cart-badge" id="nav-cart-count">0</span>
            </a>
            <a href="/login.html" class="btn btn-outline btn-sm">Login</a>
            <a href="/register.html" class="btn btn-primary btn-sm">Sign Up</a>
        `;
    } else {
        const displayName = user?.name ? user.name.split(' ')[0] : 'Account';
        navActions.innerHTML = `
            ${!isAdmin ? `
            <a href="/cart.html" class="cart-btn" aria-label="Cart">
                🛒 Cart <span class="cart-badge" id="nav-cart-count">0</span>
            </a>
            ` : ''}
            <span style="font-size: 0.9rem; font-weight: 600; color: var(--text-secondary);">
                ${isAdmin ? '👑 Admin: ' : '👤 '}${displayName}
            </span>
            <button id="logout-btn" class="btn btn-secondary btn-sm">Logout</button>
        `;

        document.getElementById('logout-btn')?.addEventListener('click', () => {
            authStorage.logout();
        });
    }

    refreshCartCount();
}

// Auto-initialize navbar on DOM load
document.addEventListener('DOMContentLoaded', () => {
    renderNavigation();
});
