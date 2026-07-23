# 🏠 BachHome - Website bán đồ gia dụng

**BachHome** là website thương mại điện tử chuyên bán **đồ gia dụng** (nhà bếp, điện gia dụng, dọn dẹp nhà cửa, phòng tắm, phòng ngủ). Hệ thống được xây dựng theo kiến trúc **Microservices** kết hợp **Domain-Driven Design (DDD)**, backend Spring Boot (Java 17) + Eureka + API Gateway + RabbitMQ + PostgreSQL, frontend React (Vite + TailwindCSS).

Ví dụ sản phẩm: Nồi cơm điện, Máy xay sinh tố, Chảo chống dính, Quạt điện, Máy hút bụi...

---

## ✨ Tính năng

### Khách hàng (Customer App)
- Đăng ký / đăng nhập bằng JWT
- Duyệt **danh mục sản phẩm** đồ gia dụng theo nhóm (nhà bếp, điện gia dụng, dọn dẹp, phòng tắm, phòng ngủ)
- **Tìm kiếm, lọc & sắp xếp** sản phẩm (theo danh mục, giá, tên...)
- **Phân trang** danh sách sản phẩm
- **Giỏ hàng** (thêm / sửa số lượng / xoá)
- **Yêu thích (wishlist)** sản phẩm
- **Đặt hàng** và theo dõi trạng thái đơn (cập nhật realtime qua WebSocket)
- **Thanh toán COD hoặc chuyển khoản QR** (tích hợp SePay)
- **Đánh giá** sản phẩm đã mua

### Quản trị (Admin Panel)
- **Tổng quan** — dashboard thống kê doanh thu, đơn hàng, sản phẩm
- **Đơn hàng** — xem và cập nhật trạng thái đơn
- **Sản phẩm** — thêm / sửa / xoá sản phẩm
- **Danh mục** — quản lý danh mục sản phẩm
- **Tồn kho** — quản lý số lượng tồn kho
- **Người dùng** — quản lý tài khoản khách hàng và phân quyền

---

## 🏗️ Kiến trúc

```
                    ┌──────────────┐   ┌──────────────┐
                    │ Customer App │   │ Admin Panel  │
                    │  (Port 3000) │   │ (Port 3002)  │
                    └──────┬───────┘   └──────┬───────┘
                           └──────────┬───────┘
                                      ▼
                              ┌──────────────┐
                              │ API Gateway  │
                              │ (Port 8080)  │
                              └──────┬───────┘
                     ┌───────────────┼───────────────┐
                     ▼               ▼               ▼
             ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
             │Eureka Server │ │  RabbitMQ    │ │ PostgreSQL   │
             │ (Port 8761)  │ │ (Port 5672)  │ │ (Port 5432+) │
             └──────────────┘ └──────────────┘ └──────────────┘
                     ▲
   ┌─────────────────┴────────────────────────────────────────┐
   │                    MICROSERVICES                          │
   │  Auth  Menu  Order  Payment  Inventory  Notify  Socket    │
   │  8081  8082  8083    8084      8085       8086    8089     │
   └───────────────────────────────────────────────────────────┘
```

## 📦 Danh sách service

| Service | Port | Mô tả |
|---------|------|-------|
| eureka-server | 8761 | Service Discovery |
| api-gateway | 8080 | API Gateway & Routing |
| service-auth | 8081 | Xác thực & phân quyền (JWT) |
| service-menu | 8082 | Sản phẩm & danh mục |
| service-order | 8083 | Đơn hàng & giỏ hàng |
| service-payment | 8084 | Thanh toán (COD / QR SePay) |
| service-inventory | 8085 | Tồn kho |
| service-notification | 8086 | Thông báo |
| service-socket | 8089 | Cập nhật realtime (WebSocket) |

---

## 🛠️ Công nghệ

**Backend:** Java 17, Spring Boot 3.2.1, Spring Cloud 2023.0.0, Spring Cloud Gateway, Netflix Eureka, Spring Data JPA, Spring Security + JWT, RabbitMQ, PostgreSQL 15, Docker & Docker Compose.

**Frontend:** React, Vite, TailwindCSS, Recharts (biểu đồ dashboard).

---

## 🚀 Cài đặt & chạy

### Yêu cầu
- **Docker Desktop** (đã bao gồm Docker Compose) — *không cần cài Java/Maven*, Docker sẽ tự build backend.
- **Node.js 18+** (để chạy frontend Customer & Admin)
- **Git**

### 1. Clone dự án
```bash
git clone <URL-repo>
cd Bach
```

### 2. Chạy backend bằng Docker Compose
```bash
# Tại thư mục gốc dự án
docker compose up -d --build
```
Lệnh này build và khởi động Eureka, API Gateway, toàn bộ microservices, RabbitMQ và các PostgreSQL. Lần đầu chờ khoảng **1–2 phút** để các service build và đăng ký xong với Eureka.

> 💡 **Dữ liệu mẫu tự nạp:** Khi backend khởi động lần đầu trên database rỗng, hệ thống tự động nạp dữ liệu mẫu — **112 sản phẩm** (5 danh mục), **17 đơn hàng** kèm chi tiết, và **tồn kho** — thông qua cơ chế `data.sql` của Spring Boot. Không cần chạy seed thủ công. Cơ chế nạp là *idempotent*: các lần khởi động sau sẽ bỏ qua nếu dữ liệu đã tồn tại (chạy `docker compose down -v` nếu muốn reset và nạp lại).

### 3. Chạy Customer App
```bash
cd frontend
npm install
npm run dev
```
Truy cập tại **http://localhost:3000**

### 4. Chạy Admin Panel
```bash
cd frontend-admin
npm install
npm run dev
```
Truy cập tại **http://localhost:3002**

---

## 🌐 URL truy cập

| Thành phần | URL |
|------------|-----|
| Customer App | http://localhost:3000 |
| Admin Panel | http://localhost:3002 |
| API Gateway | http://localhost:8080 |
| Eureka Dashboard | http://localhost:8761 |
| RabbitMQ Management | http://localhost:15672 (admin / admin) |

### Tài khoản quản trị demo
- Email: `admin@bachhome.com`
- Mật khẩu: `admin123`

---

## 📁 Cấu trúc thư mục

```
Bach/
├── eureka-server/          # Service Discovery
├── api-gateway/            # API Gateway
├── service-auth/           # Xác thực & phân quyền
├── service-menu/           # Sản phẩm & danh mục
├── service-order/          # Đơn hàng & giỏ hàng
├── service-payment/        # Thanh toán
├── service-inventory/      # Tồn kho
├── service-notification/   # Thông báo
├── service-socket/         # Realtime (WebSocket)
├── frontend/               # Customer App (React + Vite)
├── frontend-admin/         # Admin Panel (React + Vite)
├── k8s/                    # Kubernetes manifests
└── docker-compose.yml      # Docker Compose
```

Mỗi microservice tổ chức theo cấu trúc DDD 4 tầng: `interfaces/` (REST controllers) → `application/` (use case, DTO) → `domain/` (model, repository, service) → `infrastructure/` (persistence, messaging, config).

---

## 🧪 Docker Commands

```bash
# Build và start tất cả service
docker-compose up -d --build

# Xem logs
docker-compose logs -f

# Dừng tất cả service
docker-compose down

# Xoá volume (reset database)
docker-compose down -v
```

---

**Version:** 1.0.0 · **Status:** In Development
