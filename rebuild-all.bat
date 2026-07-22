@echo off
echo ========================================
echo   REBUILD ALL SERVICES
echo ========================================
echo.
echo This will rebuild all Docker images.
echo This may take 5-10 minutes.
echo.
pause

echo.
echo Stopping all services...
docker-compose down

echo.
echo Rebuilding all services...
docker-compose build --no-cache

echo.
echo Starting services...
docker-compose up -d

echo.
echo ========================================
echo   REBUILD COMPLETE!
echo ========================================
echo.
echo Services are starting. Wait 30-60 seconds before accessing.
echo.
pause
