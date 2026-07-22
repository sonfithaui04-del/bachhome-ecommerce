@echo off
echo ========================================
echo   Starting Frontend Applications
echo ========================================
echo.

REM Check if node_modules exists
if not exist "frontend\node_modules" (
    echo Installing Customer App dependencies...
    cd frontend
    call npm install
    cd ..
)

if not exist "frontend-admin\node_modules" (
    echo Installing Admin Panel dependencies...
    cd frontend-admin
    call npm install
    cd ..
)

echo.
echo Starting Customer App (Port 3000)...
start "Customer App" cmd /k "cd frontend && npm run dev"

echo.
echo Waiting 5 seconds before starting Admin Panel...
timeout /t 5 /nobreak

echo.
echo Starting Admin Panel (Port 3002)...
start "Admin Panel" cmd /k "cd frontend-admin && npm run dev"

echo.
echo ========================================
echo   Frontend Applications Started!
echo ========================================
echo.
echo  Customer App:  http://localhost:3000
echo  Admin Panel:   http://localhost:3002
echo.
echo Two new CMD windows have been opened.
echo Close those windows to stop the frontend apps.
echo.
