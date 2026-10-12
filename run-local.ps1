# ==============================================================================
# Online Food Ordering System - Local Startup Script
# ==============================================================================

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   🍕 CraveBite - Online Food Ordering System Startup    " -ForegroundColor Yellow
Write-Host "==========================================================" -ForegroundColor Cyan

# Check if Docker is available to launch MongoDB
$dockerInstalled = Get-Command docker -ErrorAction SilentlyContinue

if ($dockerInstalled) {
    Write-Host "`nChecking MongoDB container..." -ForegroundColor Cyan
    $mongoRunning = docker ps --filter "name=food_ordering_mongodb" --format "{{.Names}}"
    if (-not $mongoRunning) {
        Write-Host "Starting MongoDB via Docker Compose..." -ForegroundColor Green
        docker-compose up -d mongodb
    } else {
        Write-Host "MongoDB container is already running." -ForegroundColor Green
    }
} else {
    Write-Host "`nNote: Docker not detected. Please ensure MongoDB is running on localhost:27017 or provide MONGODB_URI." -ForegroundColor Yellow
}

# Set bootstrap environment variables for local testing
$env:ADMIN_EMAIL = "admin@foodordering.com"
$env:ADMIN_PASSWORD = "AdminPassword123!"
$env:ADMIN_NAME = "Executive Administrator"
$env:SERVER_PORT = "8080"

Write-Host "`nStarting Spring Boot Application..." -ForegroundColor Cyan
Write-Host "Customer Storefront: http://localhost:8080/" -ForegroundColor Green
Write-Host "Admin Portal:        http://localhost:8080/admin-login.html" -ForegroundColor Green
Write-Host "Admin Credentials:   admin@foodordering.com / AdminPassword123!" -ForegroundColor Yellow
Write-Host "==========================================================`n" -ForegroundColor Cyan

# Launch Maven Spring Boot runner
.\mvnw.cmd spring-boot:run
