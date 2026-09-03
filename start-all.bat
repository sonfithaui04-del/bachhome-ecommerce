@echo off
chcp 65001 >nul
echo ========================================
echo   BachHome - Khoi dong toan bo he thong
echo ========================================
echo.

echo [1/3] Khoi dong Backend (Docker)...
call start-backend.bat

echo.
echo [2/3] Doi backend dang ky voi Eureka (60 giay)...
timeout /t 60 /nobreak

echo.
echo [3/3] Khoi dong giao dien...
call start-frontend.bat

echo.
echo ========================================
echo   DA KHOI DONG XONG
echo ========================================
echo.
echo Giao dien:
echo  - Trang khach hang: http://localhost:3000
echo  - Trang quan tri:   http://localhost:3002
echo.
echo Tai khoan quan tri:
echo  - Email:    admin@bachhome.com
echo  - Mat khau: admin123
echo.
echo Ghi chu: cong cua API Gateway tren may nay do docker-compose.override.yml
echo quyet dinh (xem danh sach o buoc 1). Hai app giao dien tu doc cong nay
echo trong file .env.local nen khong can chinh gi them.
echo.
echo Nhan phim bat ky de dong cua so nay...
pause
