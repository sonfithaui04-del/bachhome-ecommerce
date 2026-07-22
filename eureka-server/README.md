# Eureka Server

## 📋 Mô tả
Service Discovery Server cho hệ thống Food Ordering. Tất cả các microservices sẽ đăng ký với Eureka Server này để có thể tìm kiếm và giao tiếp với nhau.

## 🔧 Công nghệ
- Spring Boot 3.2.1
- Spring Cloud Netflix Eureka Server 2023.0.0
- Java 17

## 🚀 Cách chạy

### Development
```bash
mvn spring-boot:run
```

### Production (với Docker)
```bash
docker build -t eureka-server:1.0.0 .
docker run -p 8761:8761 eureka-server:1.0.0
```

## 🌐 Endpoints

| Endpoint | Mô tả |
|----------|-------|
| http://localhost:8761 | Eureka Dashboard UI |
| http://localhost:8761/eureka/apps | Registry Information (XML) |
| http://localhost:8761/actuator/health | Health Check |

## ⚙️ Cấu hình

- **Port**: 8761
- **Self-Preservation**: Disabled (Development)
- **Eviction Interval**: 3 seconds

## 📊 Monitoring

Truy cập Eureka Dashboard tại: http://localhost:8761

Ở đây bạn có thể thấy:
- Danh sách tất cả services đã đăng ký
- Trạng thái health của từng service
- Instance information

## 🔗 Services đăng ký với Eureka

Dự kiến các services sau sẽ đăng ký:
- api-gateway
- service-auth
- service-menu
- service-order
- service-inventory
- service-payment
- service-notification

## 📝 Notes

- Eureka Server không đăng ký chính nó như một client
- Self-preservation mode được tắt trong development để dễ dàng testing
- Trong production, nên bật self-preservation để tránh việc loại bỏ nhầm services khi có network issues
