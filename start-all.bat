@echo off
echo ========================================
echo   FOOD ORDERING SYSTEM - START ALL
echo ========================================
echo.

echo [1/3] Starting Backend Services (Docker)...
call start-backend.bat

echo.
echo [2/3] Waiting for services to be ready (30s)...
timeout /t 30 /nobreak

echo.
echo [3/3] Starting Frontend Applications...
call start-frontend.bat

echo.
echo ========================================
echo   ALL SERVICES STARTED!
echo ========================================
echo.
echo Backend Services:
echo  - Eureka Server:    http://localhost:8761
echo  - API Gateway:      http://localhost:8080
echo  - Service Auth:     http://localhost:8081
echo  - Service Menu:     http://localhost:8082
echo  - Service Order:    http://localhost:8083
echo  - Service Inventory: http://localhost:8085
echo  - Service Notification: http://localhost:8086
echo  - Service Socket:   http://localhost:8089
echo.
echo Frontend Applications:
echo  - Customer App:     http://localhost:3000
echo  - Admin Panel:      http://localhost:3002
echo.
echo Admin Login:
echo  - Email: admin@foodorder.com
echo  - Password: admin123
echo.
echo Press any key to exit...
pause
