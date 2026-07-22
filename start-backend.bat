@echo off
echo ========================================
echo   Starting Backend Services (Docker)
echo ========================================
echo.

REM Check if Docker is running
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Docker is not running!
    echo Please start Docker Desktop and try again.
    pause
    exit /b 1
)

echo Docker is running. Starting services...
echo.

REM Start all Docker services
docker-compose up -d --build

echo.
echo Backend services are starting...
echo.
echo Services:
docker-compose ps

echo.
echo Backend started successfully!
echo.
echo Endpoints:
echo  - Eureka:     http://localhost:8761
echo  - API Gateway: http://localhost:8080
echo  - Auth:       http://localhost:8081
echo  - Menu:       http://localhost:8082
echo  - Order:      http://localhost:8083
echo  - Inventory:  http://localhost:8085
echo.
