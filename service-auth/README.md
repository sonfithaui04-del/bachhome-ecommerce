# Authentication Service

## 📋 Mô tả
Service xử lý authentication và authorization cho toàn bộ hệ thống Food Ordering.

## 🔧 Công nghệ
- Spring Boot 3.2.1
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT (JSON Web Token)
- Netflix Eureka Client
- Swagger/OpenAPI 3
- Java 17

## 🏗️ Kiến trúc DDD

```
service-auth/
├── domain/              # Business Logic Layer
│   ├── model/          # Entities (User, UserRole, UserStatus)
│   ├── repository/     # Repository Interfaces
│   └── service/        # Domain Services
├── application/         # Use Cases Layer
│   ├── usecase/        # RegisterUseCase, LoginUseCase
│   └── dto/            # Data Transfer Objects
├── infrastructure/      # Technical Layer
│   ├── repository/     # JPA Implementations
│   └── config/         # Security, JWT, Swagger
└── interfaces/          # API Layer
    └── controller/     # REST Controllers
```

## 🚀 Cách chạy

### Prerequisites
- Java 17
- PostgreSQL
- Maven

### 1. Cấu hình Database
Tạo database PostgreSQL:
```sql
CREATE DATABASE food_ordering_auth;
```

### 2. Chạy Service
```bash
mvn spring-boot:run
```

## 🌐 API Endpoints

### POST /auth/register
Đăng ký tài khoản mới

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "fullName": "Nguyen Van A",
  "phoneNumber": "0123456789"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "userId": 1,
  "email": "user@example.com",
  "fullName": "Nguyen Van A",
  "role": "CUSTOMER"
}
```

### POST /auth/login
Đăng nhập

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:** (giống register)

### GET /auth/health
Health check

## 📊 Swagger UI
Truy cập: http://localhost:8081/swagger-ui.html

## 🔐 JWT Configuration

- **Secret Key**: Được cấu hình trong application.yml
- **Expiration**: 24 hours
- **Algorithm**: HS256

## 📌 User Roles

| Role | Mô tả |
|------|-------|
| CUSTOMER | Khách hàng đặt món |
| ADMIN | Quản trị viên |
| STAFF | Nhân viên (dự phòng) |

## 📌 User Status

| Status | Mô tả |
|--------|-------|
| ACTIVE | Đang hoạt động |
| INACTIVE | Tạm ngưng |
| BANNED | Bị cấm |

## ⚙️ Configuration

- **Port**: 8081
- **Database**: PostgreSQL (localhost:5432)
- **Eureka Server**: http://localhost:8761/eureka/
- **Service Name**: service-auth

## 🔗 Dependencies

- Eureka Server (localhost:8761)
- PostgreSQL (localhost:5432)

## 📝 Logging Format

```
[YYYY-MM-DD HH:mm:ss] [LEVEL] [AUTH-SERVICE] [TRACE_ID] Message
```

## 🧪 Testing

```bash
# Test register
curl -X POST http://localhost:8081/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "test123",
    "fullName": "Test User",
    "phoneNumber": "0123456789"
  }'

# Test login
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "test123"
  }'
```
