@echo off
echo ========================================
echo   VIEW SERVICE LOGS
echo ========================================
echo.
echo Select a service to view logs:
echo.
echo  1. API Gateway
echo  2. Service Auth
echo  3. Service Menu
echo  4. Service Order
echo  5. Service Inventory
echo  6. Service Payment
echo  7. Service Notification
echo  8. Eureka Server
echo  9. All Services
echo  0. Exit
echo.
set /p choice="Enter your choice (0-9): "

if "%choice%"=="1" docker logs -f api-gateway
if "%choice%"=="2" docker logs -f service-auth
if "%choice%"=="3" docker logs -f service-menu
if "%choice%"=="4" docker logs -f service-order
if "%choice%"=="5" docker logs -f service-inventory
if "%choice%"=="6" docker logs -f service-payment
if "%choice%"=="7" docker logs -f service-notification
if "%choice%"=="8" docker logs -f eureka-server
if "%choice%"=="9" docker-compose logs -f
if "%choice%"=="0" exit

pause
