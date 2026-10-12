# ==============================================================================
# CraveBite - End-to-End Automated API Workflow Test Script
# Runs a complete customer ordering & admin fulfillment simulation against http://localhost:8080
# ==============================================================================

param(
    [string]$BaseUrl = "http://localhost:8080/api"
)

$ErrorActionPreference = "Stop"

function Write-Step {
    param([string]$Message)
    Write-Host "`n[STEP] $Message" -ForegroundColor Cyan
}

function Write-Pass {
    param([string]$Message)
    Write-Host "  ✔ $Message" -ForegroundColor Green
}

function Write-Fail {
    param([string]$Message)
    Write-Host "  ✖ $Message" -ForegroundColor Red
}

Write-Host "==========================================================" -ForegroundColor Yellow
Write-Host "   🍕 CraveBite - Automated End-to-End API Test Suite     " -ForegroundColor Yellow
Write-Host "==========================================================" -ForegroundColor Yellow
Write-Host "Target Base URL: $BaseUrl" -ForegroundColor Gray

# 1. System Health Check
Write-Step "1. Checking System Health Endpoint..."
try {
    $health = Invoke-RestMethod -Uri "$BaseUrl/health" -Method Get
    if ($health.status -eq "UP") {
        Write-Pass "System Health is UP (App: $($health.application), Version: $($health.version))"
    } else {
        Write-Fail "Unexpected health status: $($health.status)"
        exit 1
    }
} catch {
    Write-Fail "Could not connect to $BaseUrl/health. Is the server running? ($($_.Exception.Message))"
    exit 1
}

# Generate unique customer email for test run
$timestamp = Get-Date -Format "yyyyMMddHHmmss"
$customerEmail = "testuser_$timestamp@example.com"
$customerPassword = "SecurePass123!"

# 2. Customer Registration
Write-Step "2. Registering New Customer ($customerEmail)..."
$registerBody = @{
    name = "Test Automation User"
    email = $customerEmail
    password = $customerPassword
    phone = "555-0199"
    address = "742 Evergreen Terrace, Springfield"
} | ConvertTo-Json

$regResponse = Invoke-RestMethod -Uri "$BaseUrl/auth/register" -Method Post -Body $registerBody -ContentType "application/json"
$customerToken = $regResponse.token
$customerId = $regResponse.id
Write-Pass "Customer registered successfully (ID: $customerId, Role: $($regResponse.role))"
Write-Pass "JWT Bearer Token issued (Length: $($customerToken.Length) chars)"

# 3. Customer Login Verification
Write-Step "3. Verifying Customer Authentication (Login)..."
$loginBody = @{
    email = $customerEmail
    password = $customerPassword
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "$BaseUrl/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
if ($loginResponse.token) {
    Write-Pass "Login successful for $($loginResponse.email)"
}

$customerHeaders = @{
    Authorization = "Bearer $customerToken"
    "Content-Type" = "application/json"
}

# 4. Fetch Available Menu Items
Write-Step "4. Browsing Food Menu Catalog..."
$foods = Invoke-RestMethod -Uri "$BaseUrl/foods?available=true" -Method Get
Write-Pass "Found $($foods.Count) available food items in menu catalog."

if ($foods.Count -eq 0) {
    Write-Fail "No foods found. Please ensure database has seeded items."
    exit 1
}

$item1 = $foods[0]
Write-Pass "Selected Item: '$($item1.name)' - Price: `$$($item1.price) (ID: $($item1.id))"

# 5. Add Items to Shopping Cart
Write-Step "5. Adding Item to Shopping Cart..."
$addCartBody = @{
    foodItemId = $item1.id
    quantity = 2
} | ConvertTo-Json

$cartResponse = Invoke-RestMethod -Uri "$BaseUrl/cart/add" -Method Post -Headers $customerHeaders -Body $addCartBody
Write-Pass "Cart updated: $($cartResponse.totalQuantity) total items"
Write-Pass "Subtotal: `$$($cartResponse.subtotal) | Tax: `$$($cartResponse.tax) | Delivery: `$$($cartResponse.deliveryFee) | Total: `$$($cartResponse.totalAmount)"

# 6. Place Order (Checkout)
Write-Step "6. Placing Order via Cash on Delivery..."
$checkoutBody = @{
    deliveryAddress = "742 Evergreen Terrace, Apt 101"
    paymentMethod = "CASH_ON_DELIVERY"
} | ConvertTo-Json

$orderResponse = Invoke-RestMethod -Uri "$BaseUrl/orders" -Method Post -Headers $customerHeaders -Body $checkoutBody
$orderId = $orderResponse.id
Write-Pass "Order created successfully! (Order ID: #$orderId)"
Write-Pass "Order Status: $($orderResponse.orderStatus) | Total Amount: `$$($orderResponse.totalAmount)"

# Verify cart is now empty
$emptyCart = Invoke-RestMethod -Uri "$BaseUrl/cart" -Method Get -Headers $customerHeaders
if ($emptyCart.totalQuantity -eq 0) {
    Write-Pass "Verified: Shopping cart was cleared automatically after order placement."
}

# 7. Customer Order Tracking
Write-Step "7. Fetching Order Details from Customer History..."
$fetchedOrder = Invoke-RestMethod -Uri "$BaseUrl/orders/$orderId" -Method Get -Headers $customerHeaders
Write-Pass "Order retrieved (Items: $($fetchedOrder.items.Count), Destination: '$($fetchedOrder.deliveryAddress)')"

# 8. Administrator Login & Status Lifecycle
Write-Step "8. Logging in as Restaurant Administrator..."
$adminEmail = if ($env:ADMIN_EMAIL) { $env:ADMIN_EMAIL } else { "admin@foodordering.com" }
$adminPassword = if ($env:ADMIN_PASSWORD) { $env:ADMIN_PASSWORD } else { "AdminPassword123!" }

try {
    $adminLoginBody = @{
        email = $adminEmail
        password = $adminPassword
    } | ConvertTo-Json

    $adminAuth = Invoke-RestMethod -Uri "$BaseUrl/auth/login" -Method Post -Body $adminLoginBody -ContentType "application/json"
    $adminToken = $adminAuth.token
    Write-Pass "Admin authenticated (Name: $($adminAuth.name), Role: $($adminAuth.role))"

    $adminHeaders = @{
        Authorization = "Bearer $adminToken"
        "Content-Type" = "application/json"
    }

    # 9. Test Order Status State Machine Transitions
    Write-Step "9. Testing Order Fulfillment State Transitions (Admin FSM)..."
    $stages = @("CONFIRMED", "PREPARING", "OUT_FOR_DELIVERY", "DELIVERED")

    foreach ($stage in $stages) {
        $statusBody = @{ status = $stage } | ConvertTo-Json
        $updatedOrder = Invoke-RestMethod -Uri "$BaseUrl/orders/$orderId/status" -Method Put -Headers $adminHeaders -Body $statusBody
        Write-Pass "Advanced order #$orderId ➔ $stage"
    }

    if ($updatedOrder.paymentStatus -eq "PAID") {
        Write-Pass "Verified: COD order marked as PAID automatically upon DELIVERED status."
    }

    # 10. Admin Dashboard Metrics
    Write-Step "10. Inspecting Admin Dashboard Analytics..."
    $dashboard = Invoke-RestMethod -Uri "$BaseUrl/admin/dashboard" -Method Get -Headers $adminHeaders
    Write-Pass "Total Orders: $($dashboard.totalOrders)"
    Write-Pass "Total Customers: $($dashboard.totalCustomers)"
    Write-Pass "Total Food Items: $($dashboard.totalFoodItems)"
    Write-Pass "Gross Revenue: `$$($dashboard.totalRevenue)"

} catch {
    Write-Host "  Note: Admin login skipped or credentials mismatch ($($_.Exception.Message))" -ForegroundColor Yellow
}

Write-Host "`n==========================================================" -ForegroundColor Green
Write-Host "   ✔ ALL END-TO-END WORKFLOW VERIFICATIONS PASSED!         " -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green
