@echo off
echo ========================================
echo   STOPPING ALL SERVICES
echo ========================================
echo.

echo [1/2] Stopping Docker Services...
docker-compose down

echo.
echo [2/2] Stopping Frontend Applications...
echo Please close the CMD windows running:
echo  - Customer App
echo  - Admin Panel
echo.
echo Or press Ctrl+C in those windows.
echo.

echo ========================================
echo   ALL SERVICES STOPPED
echo ========================================
echo.
pause
