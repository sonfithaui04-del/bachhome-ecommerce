@echo off
chcp 65001 >nul
echo ========================================
echo   BachHome - Khoi dong Backend (Docker)
echo ========================================
echo.

REM Kiem tra Docker da chay chua
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo LOI: Docker chua chay!
    echo Hay mo Docker Desktop roi chay lai file nay.
    pause
    exit /b 1
)

echo Docker da san sang. Dang khoi dong cac service...
echo.

REM Khoi dong toan bo service. Neu vua sua code Java thi chay rebuild-all.bat
docker compose up -d

echo.
echo Trang thai container:
docker compose ps

echo.
echo Cac dia chi (cong that su dang mo tren may nay):
for /f "tokens=2 delims=:" %%p in ('docker compose port api-gateway 8080 2^>nul') do echo  - API Gateway:  http://localhost:%%p
for /f "tokens=2 delims=:" %%p in ('docker compose port eureka-server 8761 2^>nul') do echo  - Eureka:       http://localhost:%%p
for /f "tokens=2 delims=:" %%p in ('docker compose port rabbitmq 15672 2^>nul') do echo  - RabbitMQ:     http://localhost:%%p
echo.
echo Backend can khoang 60-90 giay de dang ky xong voi Eureka.
echo Neu dat hang bao loi 503 thi doi them mot lat roi thu lai.
echo.
