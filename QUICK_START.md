# 🚀 Quick Start Guide - Food Ordering System

## 📋 Yêu cầu hệ thống

### Bắt buộc
- ✅ **Java 17** hoặc cao hơn
- ✅ **Maven 3.8+**
- ✅ **Docker** và **Docker Compose**
- ✅ **Git**

### Khuyến nghị
- IntelliJ IDEA hoặc VS Code
- Postman hoặc Insomnia (để test API)
- 8GB RAM trở lên

---

## 🎯 Các bước bắt đầu

### Bước 1: Clone Project

```bash
git clone <repository-url>
cd food-ordering
```

### Bước 2: Kiểm tra cấu trúc

```bash
ls -la
```

Bạn sẽ thấy:
```
food-ordering/
├── eureka-server/          ✅ Service Discovery
├── api-gateway/            ✅ API Gateway
├── service-auth/           ✅ Authentication Service
├── service-menu/           🔨 (Coming soon)
├── service-order/          🔨 (Coming soon)
├── docker-compose.yml      ✅ Docker configuration
├── README.md               📖 Documentation
└── PROJECT_RULES_AND_STANDARDS.md
```

### Bước 3: Build tất cả services

**Option A: Build với Maven (không cần Docker)**

```bash
# Build Eureka Server
cd eureka-server
mvn clean install
cd ..

# Build API Gateway
cd api-gateway
mvn clean install
cd ..

# Build Auth Service
cd service-auth
mvn clean install
cd ..
```

**Option B: Để Docker build (khuyến nghị)**

Skip bước này, Docker sẽ tự build khi chạy `docker-compose up`.

---

### Bước 4: Chạy hệ thống với Docker

```bash
# Từ thư mục gốc food-ordering
docker-compose up -d
```

**Giải thích:**
- `-d`: Chạy ở background (detached mode)
- Docker sẽ tự động:
  - Pull PostgreSQL, RabbitMQ images
  - Build các Spring Boot services
  - Khởi động theo đúng thứ tự (dependencies)

**Chờ ~2-3 phút** để tất cả services khởi động hoàn tất.

---

### Bước 5: Kiểm tra Services

#### 5.1. Xem logs

```bash
# Xem logs tất cả services
docker-compose logs -f

# Xem logs của service cụ thể
docker-compose logs -f eureka-server
docker-compose logs -f api-gateway
docker-compose logs -f service-auth
```

#### 5.2. Kiểm tra health

```bash
# Eureka Server
curl http://localhost:8761/actuator/health

# API Gateway
curl http://localhost:8080/actuator/health

# Auth Service
curl http://localhost:8081/actuator/health
```

Tất cả phải trả về: `{"status":"UP"}`

#### 5.3. Truy cập Eureka Dashboard

Mở browser: **http://localhost:8761**

Bạn sẽ thấy các services đã đăng ký:
- API-GATEWAY
- SERVICE-AUTH

---

## 🧪 Test API

### 1. Test Register (Đăng ký user mới)

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123",
    "fullName": "John Doe",
    "phoneNumber": "0123456789"
  }'
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "userId": 1,
  "email": "john@example.com",
  "fullName": "John Doe",
  "role": "CUSTOMER"
}
```

### 2. Test Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'
```

**Expected Response:** (giống register)

### 3. Test với Swagger UI

Mở browser: **http://localhost:8081/swagger-ui.html**

Tại đây bạn có thể:
- Xem tất cả endpoints
- Test API trực tiếp trên UI
- Xem request/response schema

---

## 📊 Monitoring & Management

### RabbitMQ Management UI

**URL:** http://localhost:15672  
**Username:** admin  
**Password:** admin

Tại đây bạn có thể:
- Xem queues
- Xem messages
- Monitor performance

### PostgreSQL Database

**Connection Info:**
```
Host: localhost
Port: 5432 (auth), 5433 (menu), 5434 (order)
Username: postgres
Password: postgres
Database: food_ordering_auth
```

Kết nối bằng pgAdmin hoặc DBeaver để xem dữ liệu.

---

## 🛠️ Development Workflow

### Chạy một service riêng lẻ (không dùng Docker)

**Prerequisites:**
- PostgreSQL đang chạy ở localhost:5432
- Eureka Server đang chạy ở localhost:8761

```bash
cd service-auth
mvn spring-boot:run
```

### Rebuild một service cụ thể

```bash
# Rebuild và restart service-auth
docker-compose up -d --build service-auth

# Xem logs
docker-compose logs -f service-auth
```

### Stop và xóa tất cả

```bash
# Stop tất cả services
docker-compose down

# Stop và xóa volumes (reset databases)
docker-compose down -v
```

---

## ❓ Troubleshooting

### Problem 1: Port already in use

**Error:** `Bind for 0.0.0.0:8080 failed: port is already allocated`

**Solution:**
```bash
# Tìm process đang dùng port
netstat -ano | findstr :8080  # Windows
lsof -i :8080                  # Linux/Mac

# Kill process
taskkill /PID <PID> /F         # Windows
kill -9 <PID>                  # Linux/Mac
```

### Problem 2: Service không đăng ký với Eureka

**Solution:**
1. Kiểm tra Eureka Server đã chạy chưa: http://localhost:8761
2. Xem logs của service: `docker-compose logs -f service-auth`
3. Restart service: `docker-compose restart service-auth`

### Problem 3: Database connection error

**Solution:**
1. Kiểm tra PostgreSQL container: `docker-compose ps`
2. Kiểm tra logs: `docker-compose logs -f postgres-auth`
3. Restart database: `docker-compose restart postgres-auth`

### Problem 4: Build failed

**Solution:**
```bash
# Clean và rebuild
cd service-auth
mvn clean install -U

# Nếu vẫn lỗi, xóa .m2 cache
rm -rf ~/.m2/repository/com/foodordering
```

---

## 📚 Next Steps

Sau khi hệ thống chạy thành công:

1. ✅ Đọc [PROJECT_RULES_AND_STANDARDS.md](PROJECT_RULES_AND_STANDARDS.md) để hiểu quy tắc code
2. ✅ Xem [todolist.md](todolist.md) để biết roadmap
3. ✅ Tạo branch mới: `git checkout -b feature/menu/your-feature`
4. ✅ Bắt đầu develop service tiếp theo (Menu, Order, etc.)

---

## 🎓 Học thêm về kiến trúc

- **Microservices Pattern**: https://microservices.io/
- **Domain-Driven Design**: https://martinfowler.com/bliki/DomainDrivenDesign.html
- **Spring Cloud**: https://spring.io/projects/spring-cloud

---

## 📞 Support

Nếu gặp vấn đề:
1. Check logs: `docker-compose logs -f`
2. Check health: `curl http://localhost:8081/actuator/health`
3. Tham khảo README.md của từng service
4. Contact team

---

**Happy Coding! 🚀**
