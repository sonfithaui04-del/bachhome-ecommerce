# 📜 HƯỚNG DẪN SỬ DỤNG CÁC SCRIPT

## 🚀 **KHỞI ĐỘNG HỆ THỐNG**

### **Cách 1: Khởi động tất cả (Recommended)**
```cmd
start-all.bat
```
Script này sẽ:
1. ✅ Khởi động tất cả Docker services (backend)
2. ✅ Đợi 30 giây để services sẵn sàng
3. ✅ Khởi động Customer App (port 3000)
4. ✅ Khởi động Admin Panel (port 3002)

### **Cách 2: Khởi động từng phần**

**Chỉ khởi động Backend:**
```cmd
start-backend.bat
```

**Chỉ khởi động Frontend:**
```cmd
start-frontend.bat
```

---

## 🛑 **DỪNG HỆ THỐNG**

```cmd
stop-all.bat
```

Dừng tất cả Docker services. Frontend apps cần đóng CMD windows thủ công.

---

## 🔨 **REBUILD TOÀN BỘ**

```cmd
rebuild-all.bat
```

Dùng khi:
- Có thay đổi code backend
- Cần build lại từ đầu
- Fix lỗi

**Lưu ý:** Mất 5-10 phút!

---

## 📊 **XEM LOGS**

```cmd
view-logs.bat
```

Chọn service cần xem logs:
- API Gateway
- Service Auth
- Service Menu
- Service Order
- Service Inventory
- Hoặc tất cả

**Shortcut:** `Ctrl+C` để thoát

---

## 🌐 **ENDPOINTS SAU KHI CHẠY**

### **Backend (Docker):**
- 🌍 Eureka Server: http://localhost:8761
- 🚪 API Gateway: http://localhost:8080
- 🔐 Service Auth: http://localhost:8081
- 🍔 Service Menu: http://localhost:8082
- 📦 Service Order: http://localhost:8083
- 📊 Service Inventory: http://localhost:8085
- 💳 Service Payment: http://localhost:8084
- 🔔 Service Notification: http://localhost:8086

### **Frontend (npm):**
- 🛒 Customer App: http://localhost:3000
- 👨‍💼 Admin Panel: http://localhost:3002

### **Swagger UI:**
- Auth: http://localhost:8081/swagger-ui.html
- Menu: http://localhost:8082/swagger-ui.html
- Order: http://localhost:8083/swagger-ui.html
- Inventory: http://localhost:8085/swagger-ui.html

---

## 👤 **TÀI KHOẢN ĐĂNG NHẬP**

### **Admin Panel:**
- Email: `admin@foodorder.com`
- Password: `admin123`

### **Customer App:**
- Đăng ký tài khoản mới hoặc dùng:
- Email: `tuannguyen10112004@gmail.com`
- Password: `tuan1011`

---

## 🔧 **TROUBLESHOOTING**

### **Lỗi: "Docker is not running"**
➡️ Mở Docker Desktop và đợi cho đến khi hiện "Docker is running"

### **Lỗi: Port đã được sử dụng**
➡️ Dừng các ứng dụng khác đang dùng port 3000, 3002, 8080-8086

### **Frontend không start**
➡️ Chạy manual:
```cmd
cd frontend
npm install
npm run dev
```

```cmd
cd frontend-admin
npm install
npm run dev
```

### **Services không sẵn sàng**
➡️ Đợi 1-2 phút sau khi chạy `start-all.bat`
➡️ Hoặc chạy `view-logs.bat` để xem lỗi

---

## 📁 **CẤU TRÚC SCRIPTS**

```
food-ordering/
├── start-all.bat          # 🚀 Chạy tất cả
├── start-backend.bat      # 🐳 Chỉ backend
├── start-frontend.bat     # 💻 Chỉ frontend
├── stop-all.bat           # 🛑 Dừng tất cả
├── rebuild-all.bat        # 🔨 Rebuild Docker
└── view-logs.bat          # 📊 Xem logs
```

---

## ⚡ **QUICK START**

1. Mở **Docker Desktop**
2. Double-click **`start-all.bat`**
3. Đợi 1-2 phút
4. Truy cập:
   - Customer: http://localhost:3000
   - Admin: http://localhost:3002

**Done!** 🎉

---

## 🎯 **WORKFLOW LÀM VIỆC HÀNG NGÀY**

### **Buổi sáng - Bắt đầu làm việc:**
```cmd
start-all.bat
```

### **Khi có thay đổi code backend:**
```cmd
rebuild-all.bat
```

### **Khi debug:**
```cmd
view-logs.bat
```

### **Cuối ngày - Tắt máy:**
```cmd
stop-all.bat
```

---

**Chúc bạn làm việc hiệu quả!** 💪✨
