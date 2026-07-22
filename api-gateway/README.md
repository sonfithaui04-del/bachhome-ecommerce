# API Gateway

## 📋 Mô tả
API Gateway là cổng vào duy nhất cho tất cả requests từ client đến hệ thống Food Ordering. Gateway sẽ routing requests đến các microservices tương ứng thông qua Eureka Service Discovery.

## 🔧 Công nghệ
- Spring Boot 3.2.1
- Spring Cloud Gateway 2023.0.0
- Netflix Eureka Client
- Spring Cloud LoadBalancer
- Java 17

## 🚀 Cách chạy

```bash
mvn spring-boot:run
```

## 🌐 Route Configuration

| Client Request | Target Service | Internal Path |
|----------------|----------------|---------------|
| `/api/auth/**` | service-auth | `/**` |
| `/api/menu/**` | service-menu | `/**` |
| `/api/orders/**` | service-order | `/**` |
| `/api/inventory/**` | service-inventory | `/**` |
| `/api/payments/**` | service-payment | `/**` |
| `/api/notifications/**` | service-notification | `/**` |

## 📝 Load Balancing

Gateway sử dụng `lb://` protocol để tự động load balance giữa các instances của cùng một service thông qua Eureka.

## 🔒 CORS Configuration

CORS đã được cấu hình để cho phép:
- All origins (*)
- Methods: GET, POST, PUT, DELETE, OPTIONS
- All headers

**Note**: Trong production, nên giới hạn `allowedOrigins` đến domain cụ thể.

## 📊 Monitoring Endpoints

| Endpoint | Mô tả |
|----------|-------|
| http://localhost:8080/actuator/health | Health check |
| http://localhost:8080/actuator/gateway/routes | Danh sách routes |

## 🔗 Dependencies

Gateway cần Eureka Server chạy tại: http://localhost:8761

## 📌 Example Requests

```bash
# Auth - Register
POST http://localhost:8080/api/auth/register

# Menu - Get all items
GET http://localhost:8080/api/menu/items

# Orders - Create order
POST http://localhost:8080/api/orders/create
```

## ⚙️ Configuration

- **Port**: 8080
- **Eureka Server**: http://localhost:8761/eureka/
- **Service Name**: api-gateway
