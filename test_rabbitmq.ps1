$baseUrl = "http://localhost:8080"

# 1. Login
$loginBody = @{
    email = "admin@foodorder.com"
    password = "admin123"
} | ConvertTo-Json

Write-Host "Logging in..."
try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.token
    $userId = $loginResponse.userId
    Write-Host "Login successful. Token: $token, UserId: $userId"
} catch {
    Write-Error "Login failed: $_"
    exit 1
}

# 2. Create Order
$orderBody = @{
    userId = $userId
    items = @(
        @{
            menuItemId = 1
            quantity = 2
            price = 50000
            menuItemName = "Test Item"
        }
    )
    deliveryAddress = "123 Test St"
    phoneNumber = "0123456789"
} | ConvertTo-Json

Write-Host "Creating Order..."
$headers = @{
    Authorization = "Bearer $token"
}

try {
    $orderResponse = Invoke-RestMethod -Uri "$baseUrl/api/orders" -Method Post -Body $orderBody -ContentType "application/json" -Headers $headers
    $orderId = $orderResponse.id
    Write-Host "Order created. ID: $orderId, Status: $($orderResponse.status)"
} catch {
    Write-Error "Create Order failed: $_"
    exit 1
}

# 3. Confirm Order
$confirmBody = @{
    status = "CONFIRMED"
} | ConvertTo-Json

Write-Host "Confirming Order $orderId..."
try {
    $confirmResponse = Invoke-RestMethod -Uri "$baseUrl/api/orders/$orderId/status" -Method Patch -Body $confirmBody -ContentType "application/json" -Headers $headers
    Write-Host "Order confirmed. New Status: $($confirmResponse.status)"
} catch {
    Write-Error "Confirm Order failed: $_"
    exit 1
}

Write-Host "Test completed successfully."
