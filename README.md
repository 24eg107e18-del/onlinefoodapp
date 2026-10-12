# 🍕 CraveBite — Online Food Ordering System

> A full-stack, enterprise-grade, secure, and production-ready **Online Food Ordering System** built with **Java 17 / Spring Boot 3**, **Spring Data MongoDB**, **Spring Security + Stateless JWT**, and a modern, responsive **Vanilla HTML5/CSS3/JavaScript** frontend.

---

## 📋 Table of Contents
1. [Project Abstract](#1-project-abstract)
2. [Key Features](#2-key-features)
3. [Technology Stack](#3-technology-stack)
4. [System Architecture](#4-system-architecture)
5. [Project Structure](#5-project-structure)
6. [Prerequisites](#6-prerequisites)
7. [Database Setup (Local & MongoDB Atlas)](#7-database-setup)
8. [Configuration & Environment Variables](#8-configuration--environment-variables)
9. [Administrator Account Bootstrap](#9-administrator-account-bootstrap)
10. [Running the Application](#10-running-the-application)
11. [Frontend Access Instructions](#11-frontend-access-instructions)
12. [API Endpoint Documentation](#12-api-endpoint-documentation)
13. [Security, Authentication & Role Matrix](#13-security-authentication--role-matrix)
14. [Testing Suite](#14-testing-suite)
15. [Deployment Guide (Render / Cloud Platforms)](#15-deployment-guide)
16. [Troubleshooting Guide](#16-troubleshooting-guide)
17. [Future Enhancements](#17-future-enhancements)

---

## 1. Project Abstract

The **Online Food Ordering System** is an end-to-end web platform designed to streamline food browsing, ordering, fulfillment, and kitchen management. 

- **Customers** can browse dynamic restaurant menus, search food items with debounced query filters, sort by categories, view detailed nutritional and ingredient summaries, maintain persistent carts, place Cash on Delivery orders, track fulfillment states through a live stepper, and manage personal profiles.
- **Administrators** have access to a secure, role-restricted dashboard featuring key restaurant analytics (gross revenue, order counts, status pipelines), full food menu catalog CRUD operations, category management, customer registries, and valid status transition fulfillment.

The platform eliminates manual telephone orders, enforces strict server-side price recalculation and inventory snapshotting, and is suited for college project demonstrations, technical viva presentations, and real-world deployments.

---

## 2. Key Features

### 👤 Customer Experience
- **Interactive Storefront**: Modern warm cream and coral theme with responsive navigation and dynamic cart counters.
- **Menu Filtering & Instant Search**: Debounced live text search combined with category chips and in-stock toggles.
- **Dish Details**: High-resolution dish views, descriptions, price calculations, and item counter controls.
- **Persistent Cart**: Server-managed carts that validate item availability and apply conditional free shipping rules.
- **Secure Checkout**: Cash on Delivery (COD) payment workflow with prefilled profile addresses.
- **Live Order Tracker**: Multi-step visual progress tracker (`PENDING` ➔ `CONFIRMED` ➔ `PREPARING` ➔ `OUT_FOR_DELIVERY` ➔ `DELIVERED`).
- **Profile Management**: Self-service contact information and delivery address updates.

### 👑 Administrator Control Center
- **Executive Analytics**: Gross revenue calculations, total orders, active customer counts, and category statistics.
- **Status Pipeline Overview**: Live breakdown of tickets currently in preparation, transit, or delivered.
- **Menu Management**: Full CRUD capabilities to upload dishes, assign categories, set prices, and toggle availability.
- **Category Control**: Organize menu sections and toggle visibility across the storefront.
- **Customer Directory**: Secure inspection of registered customers and default delivery destinations.
- **Order Processing**: Enforced state-machine transitions preventing illegal status modifications (e.g., delivered orders cannot revert to pending).

---

## 3. Technology Stack

### Backend
- **Language**: Java 17 / 21 / 25 LTS
- **Framework**: Spring Boot 3.3.5
- **Data Access**: Spring Data MongoDB
- **Security**: Spring Security 6, BCrypt Password Encoder
- **Authentication**: Stateless JSON Web Token (JJWT 0.12.6)
- **Validation**: Jakarta Validation API (Hibernate Validator)
- **Build Tool**: Apache Maven (with integrated `mvnw` wrapper)

### Database
- **Database Engine**: MongoDB 6.0+ / MongoDB 7.0+
- **Deployments Supported**: Local MongoDB daemon, Dockerized MongoDB, or MongoDB Atlas Cloud

### Frontend
- **Markup & Styling**: HTML5, Responsive CSS3 with custom properties (CSS variables)
- **Scripting**: Modern Vanilla JavaScript (ES6+ Modules, Fetch API, Async/Await)
- **Frameworks**: Zero third-party JavaScript frameworks (lightweight, zero bloat, instant loading)

### Automated Testing
- JUnit 5
- Mockito
- Spring MockMvc
- Surefire Test Runner

---

## 4. System Architecture

```
[ Browser / Mobile Client ]
         │
         │  HTTP / JSON (Bearer JWT)
         ▼
[ Spring Security Filter Chain ]
    ├── AuthTokenFilter (Validates JWT Signature & Claims)
    └── SecurityFilterChain (Role-Based Authorization: CUSTOMER vs ADMIN)
         │
         ▼
[ REST Controllers ] ─── (DTO Validation: @Valid)
         │
         ▼
[ Service Layer ] ────── (Business Rules, Server-Side Pricing, Enforced Transitions)
         │
         ▼
[ Spring Data Repositories ]
         │
         ▼
[ MongoDB Database ] (Users, Categories, FoodItems, Carts, Orders)
```

---

## 5. Project Structure

```
online-food-ordering-system/
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .gitignore
├── .env.example
├── README.md
├── src/
│   ├── main/
│   │   ├── java/com/example/foodordering/
│   │   │   ├── FoodOrderingApplication.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── DataInitializer.java
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── FoodItemController.java
│   │   │   │   ├── CategoryController.java
│   │   │   │   ├── CartController.java
│   │   │   │   ├── OrderController.java
│   │   │   │   ├── UserController.java
│   │   │   │   ├── AdminController.java
│   │   │   │   └── HealthController.java
│   │   │   ├── dto/
│   │   │   │   ├── RegisterRequest.java
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── AuthResponse.java
│   │   │   │   ├── UserProfileResponse.java
│   │   │   │   ├── UpdateProfileRequest.java
│   │   │   │   ├── CategoryRequest.java
│   │   │   │   ├── CategoryResponse.java
│   │   │   │   ├── FoodItemRequest.java
│   │   │   │   ├── FoodItemResponse.java
│   │   │   │   ├── AddToCartRequest.java
│   │   │   │   ├── UpdateCartItemRequest.java
│   │   │   │   ├── CartItemResponse.java
│   │   │   │   ├── CartResponse.java
│   │   │   │   ├── CheckoutRequest.java
│   │   │   │   ├── OrderItemResponse.java
│   │   │   │   ├── OrderResponse.java
│   │   │   │   ├── OrderStatusUpdateRequest.java
│   │   │   │   ├── AdminDashboardResponse.java
│   │   │   │   └── ErrorResponse.java
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── DuplicateEmailException.java
│   │   │   │   ├── EmptyCartException.java
│   │   │   │   ├── InvalidOrderException.java
│   │   │   │   └── InvalidStatusTransitionException.java
│   │   │   ├── model/
│   │   │   │   ├── User.java
│   │   │   │   ├── Role.java
│   │   │   │   ├── Category.java
│   │   │   │   ├── FoodItem.java
│   │   │   │   ├── Cart.java
│   │   │   │   ├── CartItem.java
│   │   │   │   ├── Order.java
│   │   │   │   ├── OrderItem.java
│   │   │   │   ├── OrderStatus.java
│   │   │   │   └── PaymentStatus.java
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── CategoryRepository.java
│   │   │   │   ├── FoodItemRepository.java
│   │   │   │   ├── CartRepository.java
│   │   │   │   └── OrderRepository.java
│   │   │   ├── security/
│   │   │   │   ├── JwtUtils.java
│   │   │   │   ├── UserDetailsImpl.java
│   │   │   │   ├── UserDetailsServiceImpl.java
│   │   │   │   ├── AuthTokenFilter.java
│   │   │   │   ├── AuthEntryPointJwt.java
│   │   │   │   └── CustomAccessDeniedHandler.java
│   │   │   └── service/
│   │   │       ├── AuthService.java
│   │   │       ├── UserService.java
│   │   │       ├── CategoryService.java
│   │   │       ├── FoodItemService.java
│   │   │       ├── CartService.java
│   │   │       ├── OrderService.java
│   │   │       └── AdminService.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/
│   │           ├── index.html
│   │           ├── login.html
│   │           ├── register.html
│   │           ├── menu.html
│   │           ├── food-details.html
│   │           ├── cart.html
│   │           ├── checkout.html
│   │           ├── order-confirmation.html
│   │           ├── my-orders.html
│   │           ├── order-tracking.html
│   │           ├── profile.html
│   │           ├── admin-login.html
│   │           ├── admin-dashboard.html
│   │           ├── manage-food.html
│   │           ├── manage-categories.html
│   │           ├── manage-customers.html
│   │           ├── manage-orders.html
│   │           ├── css/style.css
│   │           └── js/
│   │               ├── api.js
│   │               └── main.js
│   └── test/
│       └── java/com/example/foodordering/
│           ├── controller/AuthControllerMockMvcTest.java
│           ├── security/SecurityAndJwtTest.java
│           └── service/
│               ├── AuthServiceTest.java
│               ├── CartServiceTest.java
│               ├── FoodItemServiceTest.java
│               └── OrderServiceTest.java
```

---

## 6. Prerequisites

- **Java Development Kit (JDK)**: JDK 17, 21, or 25 installed. Verify with:
  ```bash
  java -version
  ```
- **Maven**: Version 3.8+ (or use the included `./mvnw` / `mvnw.cmd` wrapper).
- **MongoDB**: Either a local MongoDB instance running on port `27017` or a free MongoDB Atlas cluster.

---

## 7. Database Setup

### Option A: Local MongoDB (Default Development)
If you have MongoDB Community Server installed locally or in Docker:
```bash
# Docker command to start local MongoDB:
docker run -d --name local-mongo -p 27017:27017 mongo:latest
```
By default, the application connects to `mongodb://localhost:27017/food_ordering_db`.

### Option B: MongoDB Atlas (Cloud / Production)
1. Log in to [MongoDB Atlas](https://www.mongodb.com/cloud/atlas).
2. Create a free shared cluster (e.g., `M0 Sandbox`).
3. Under **Security ➔ Database Access**, create a user (e.g., `foodapp_admin`) with password.
4. Under **Security ➔ Network Access**, add an IP access rule (allow your IP or `0.0.0.0/0` for cloud deployment).
5. Under **Clusters ➔ Connect ➔ Drivers**, copy your connection string:
   ```
   mongodb+srv://<username>:<password>@cluster0.abcde.mongodb.net/food_ordering_db?retryWrites=true&w=majority
   ```
6. Set the `MONGODB_URI` environment variable to this connection string before launching the app.

---

## 8. Configuration & Environment Variables

Copy `.env.example` to create your environment configuration or set environment variables in your terminal / hosting provider:

| Variable | Description | Default Local Value | Production Recommendation |
|---|---|---|---|
| `SERVER_PORT` / `PORT` | Web server listening port | `8080` | Assigned by cloud host (e.g. Render) |
| `MONGODB_URI` | MongoDB connection URI | `mongodb://localhost:27017/food_ordering_db` | MongoDB Atlas cluster connection string |
| `JWT_SECRET` | Secret key for signing tokens | Dev key (32+ bytes) | A strong 256-bit cryptographic hex key |
| `JWT_EXPIRATION_MS`| JWT lifetime in milliseconds | `86400000` (24 Hours) | `86400000` or shorter |
| `ADMIN_EMAIL` | Bootstrap admin email | Empty | `admin@yourrestaurant.com` |
| `ADMIN_PASSWORD` | Bootstrap admin password | Empty | Strong password with symbols and numbers |
| `ADMIN_NAME` | Bootstrap admin display name | `System Administrator` | Custom admin name |

---

## 9. Administrator Account Bootstrap

For security, **no default admin account with a committed password is hardcoded**.

To initialize the first Administrator account safely:
1. Provide the following environment variables:
   ```bash
   ADMIN_EMAIL=admin@foodordering.com
   ADMIN_PASSWORD=AdminSecurePass2026!
   ```
2. When the application starts up, `DataInitializer` detects these properties.
3. If an account with this email does not exist, it registers the account with the `ADMIN` role, hashes the password using BCrypt, and seeds initial sample menu items if the database is brand new.
4. If the account already exists, `DataInitializer` skips creation to avoid overwriting changes.

---

## 10. Running the Application

### Using the Maven Wrapper on Windows:
```powershell
# Set optional admin credentials for initial seed:
$env:ADMIN_EMAIL="admin@foodordering.com"
$env:ADMIN_PASSWORD="AdminPassword123!"

# Run with Spring Boot plugin
.\mvnw.cmd spring-boot:run
```

### Using Maven on Linux / macOS:
```bash
export ADMIN_EMAIL="admin@foodordering.com"
export ADMIN_PASSWORD="AdminPassword123!"

./mvnw spring-boot:run
```

### Building and Running the Executable JAR:
```bash
./mvnw clean package
java -jar target/food-ordering-system-1.0.0.jar
```

---

## 11. Frontend Access Instructions

Once the application is running:

| Page | URL | Purpose |
|---|---|---|
| **Customer Home** | `http://localhost:8080/` | Hero banner, categories, featured dishes |
| **Food Menu** | `http://localhost:8080/menu.html` | Search, category filtering, add to cart |
| **Dish Details** | `http://localhost:8080/food-details.html?id=<id>` | Detailed ingredients, quantity calculator |
| **Shopping Cart** | `http://localhost:8080/cart.html` | Adjust quantities, server total calculation |
| **Checkout** | `http://localhost:8080/checkout.html` | Delivery address form, COD payment |
| **Confirmation** | `http://localhost:8080/order-confirmation.html?id=<id>`| Order placed receipt |
| **My Orders** | `http://localhost:8080/my-orders.html` | Customer order history & receipts |
| **Order Tracking** | `http://localhost:8080/order-tracking.html?id=<id>` | Live status tracker (stepper) |
| **Customer Profile**| `http://localhost:8080/profile.html` | Contact details and saved address |
| **Admin Login** | `http://localhost:8080/admin-login.html` | Administrator portal authentication |
| **Admin Dashboard**| `http://localhost:8080/admin-dashboard.html` | Revenue, orders, statistics |
| **Manage Food** | `http://localhost:8080/manage-food.html` | Add/Edit/Delete food items |
| **Manage Categories**| `http://localhost:8080/manage-categories.html` | Add/Edit/Delete food categories |
| **Manage Orders** | `http://localhost:8080/manage-orders.html` | Order processing & status changes |
| **Manage Customers**| `http://localhost:8080/manage-customers.html` | Customer user roster |
| **Health Check** | `http://localhost:8080/api/health` | Service health status |

---

## 12. API Endpoint Documentation

### Authentication (`/api/auth`)
- `POST /api/auth/register` — Register a new customer (`CUSTOMER` role enforced)
- `POST /api/auth/login` — Login with email/password; returns JWT token

### Food Items (`/api/foods`)
- `GET /api/foods` — List foods with query filters (`search`, `category`, `available`, `page`, `size`)
- `GET /api/foods/{id}` — Get single food details
- `POST /api/foods` — **[ADMIN]** Create food item
- `PUT /api/foods/{id}` — **[ADMIN]** Update food item
- `DELETE /api/foods/{id}` — **[ADMIN]** Delete food item

### Categories (`/api/categories`)
- `GET /api/categories` — List categories (`activeOnly=true|false`)
- `GET /api/categories/{id}` — Get category by ID
- `POST /api/categories` — **[ADMIN]** Create category
- `PUT /api/categories/{id}` — **[ADMIN]** Update category
- `DELETE /api/categories/{id}` — **[ADMIN]** Delete category

### Shopping Cart (`/api/cart`)
- `GET /api/cart` — **[AUTH]** Retrieve current user's cart with server-calculated totals
- `POST /api/cart/add` — **[AUTH]** Add food item to cart
- `PUT /api/cart/{itemId}` — **[AUTH]** Update item quantity (0 removes item)
- `DELETE /api/cart/{itemId}` — **[AUTH]** Remove item from cart
- `DELETE /api/cart/clear` — **[AUTH]** Empty cart

### Orders (`/api/orders`)
- `POST /api/orders` — **[AUTH]** Place order from active cart (creates immutable price snapshots)
- `GET /api/orders` — **[AUTH]** Get current customer's order history
- `GET /api/orders/{id}` — **[AUTH]** Get single order (enforces customer ownership or admin role)
- `PUT /api/orders/{id}/status` — **[ADMIN]** Update order status (validates transition states)

### Customer Profile (`/api/users`)
- `GET /api/users/profile` — **[AUTH]** Get current user's profile
- `PUT /api/users/profile` — **[AUTH]** Update name, phone, or address

### Administrator Dashboard (`/api/admin`)
- `GET /api/admin/dashboard` — **[ADMIN]** Get revenue, order counts, status breakdown
- `GET /api/admin/orders` — **[ADMIN]** Get all customer orders (paginated)
- `GET /api/admin/users` — **[ADMIN]** Get all registered customer profiles

---

## 13. Security, Authentication & Role Matrix

| Endpoint Group | Public | CUSTOMER | ADMIN |
|---|:---:|:---:|:---:|
| Static Web Frontend | ✅ | ✅ | ✅ |
| Registration & Login | ✅ | ✅ | ✅ |
| Health Check | ✅ | ✅ | ✅ |
| Browse Foods & Categories | ✅ | ✅ | ✅ |
| Cart & Checkout | ❌ | ✅ | ✅ |
| View Own Orders & Tracking | ❌ | ✅ | ✅ |
| View Other Customers' Orders | ❌ | ❌ | ✅ |
| Food & Category Mutations | ❌ | ❌ | ✅ |
| Order Status Transitions | ❌ | ❌ | ✅ |
| Admin Dashboard Metrics | ❌ | ❌ | ✅ |

### Security Measures:
1. **Password Hashing**: Passwords hashed using `BCryptPasswordEncoder` with salt.
2. **Stateless JWT**: Signed using HMAC-SHA256 (`Keys.hmacShaKeyFor`) with secret key configured via environment variables.
3. **Data Protection**: Password hashes are never returned in DTO responses or client payloads.
4. **Server-Side Pricing**: Frontend total calculations are strictly ignored. The server looks up current food prices from MongoDB, re-verifies availability, and calculates taxes and delivery fees.
5. **Ownership Guard**: Customer A cannot retrieve Order B belonging to Customer B.
6. **Finite State Machine**: Orders cannot perform invalid state jumps. Terminal states (`DELIVERED`, `CANCELLED`) cannot be modified.

---

## 14. Testing Suite

The application includes unit and integration tests covering security, services, business rules, and REST controllers.

To run the complete test suite:
```bash
./mvnw test
```
Or on Windows:
```powershell
.\mvnw.cmd test
```

### Verified Test Cases:
- **`AuthServiceTest`**: Customer registration, password hashing verification, duplicate email rejection, login verification.
- **`FoodItemServiceTest`**: Food item creation, category existence validation, negative price rejection, search/filter logic, deletion.
- **`CartServiceTest`**: Add to cart, out-of-stock rejection, quantity updates, subtotal calculation, free shipping threshold validation, cart clearing.
- **`OrderServiceTest`**: Empty cart checkout rejection, server-side totals calculation, food price snapshotting, ownership verification, valid and invalid status transitions.
- **`SecurityAndJwtTest`**: Token generation, claims extraction, expired token rejection, tampered signature detection.
- **`AuthControllerMockMvcTest`**: MockMvc HTTP slice testing verifying 201 Created, 400 Bad Request, and 409 Conflict statuses.

---

## 15. Deployment Guide

### Deploying to Render / Heroku / Fly.io

1. **Docker / Native Java**:
   The application binds to the platform's dynamic port via:
   ```properties
   server.port=${PORT:${SERVER_PORT:8080}}
   ```
2. **Build Command**:
   ```bash
   mvn clean package -DskipTests
   ```
3. **Start Command**:
   ```bash
   java -jar target/food-ordering-system-1.0.0.jar
   ```
4. **Environment Variables on Cloud Host**:
   - `MONGODB_URI`: Your MongoDB Atlas connection string.
   - `JWT_SECRET`: A secure 32+ character random secret string.
   - `ADMIN_EMAIL`: Initial admin login email.
   - `ADMIN_PASSWORD`: Initial admin password.

---

## 16. Troubleshooting Guide

| Issue | Cause | Solution |
|---|---|---|
| `503 Service Unavailable` on API calls | MongoDB daemon is not running or URI is incorrect | Ensure MongoDB is started locally on port 27017 or verify Atlas network access / IP whitelist |
| `401 Unauthorized` on checkout | User is not logged in or token has expired | Sign in via `/login.html` to obtain a fresh JWT token |
| `403 Forbidden` on admin APIs | Signed in as a regular `CUSTOMER` | Sign in with an account having `ADMIN` role via `/admin-login.html` |
| `Compilation error with Lombok` | JDK 24/25 internal javac API incompatibility | Project uses clean standard Java POJOs and DTOs with zero reliance on Lombok compiler hacks |

---

## 17. Future Enhancements

- Integration with payment gateways (Stripe, Razorpay, PayPal) once merchant API credentials are provided.
- Real-time order status tracking via WebSocket / Server-Sent Events (SSE).
- SMS / Email notifications via Twilio or SendGrid upon order status change.
- Multi-restaurant vendor tenancy.
- Customer food ratings and review submissions.

---

## 18. Project Author & License

- **Project**: Online Food Ordering System (Full-Stack Web Application)
- **Built for**: College Final-Year Project, Live Technical Viva Demonstration & Production Evaluation
- **License**: MIT Open Source License
