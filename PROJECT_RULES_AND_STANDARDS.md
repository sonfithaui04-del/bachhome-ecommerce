# 🏗️ Project Architecture & Development Rules  
> Food Ordering System — Microservices + DDD + API Gateway + RabbitMQ

---

## 📌 1. Project Overview

Dự án xây dựng theo mô hình **Microservices kết hợp Domain-Driven Design (DDD)**.  
Mỗi service là một **bounded context độc lập**, quản lý dữ liệu riêng, triển khai độc lập và giao tiếp thông qua:

- HTTP (gateway routing)
- RabbitMQ (event-driven messaging)

Công nghệ sử dụng:

- Spring Boot 3.x  
- Spring Cloud 2023.x  
- Eureka Discovery  
- Spring Cloud Gateway  
- RabbitMQ  
- PostgreSQL/MySQL  
- Docker (khuyến nghị)

---

## 📌 2. Repository & Branch Rules

### 🔹 Branch Convention

| Loại branch | Naming rule | Example |
|------------|-------------|---------|
| Main branch | `main` | main |
| Development | `develop` | develop |
| Feature | `feature/[service]/[feature]` | feature/order/create |
| Fix | `fix/[service]/[issue]` | fix/auth/jwt-token |
| Release | `release/vX.Y.Z` | release/v1.0.0 |

---

### 🔹 Commit Message Convention (Angular Style)

```
<type>(service): message
```

| Type | Ý nghĩa |
|------|--------|
| feat | thêm chức năng mới |
| fix | sửa lỗi |
| refactor | cải tổ logic, không đổi behavior |
| docs | cập nhật tài liệu |
| test | thêm test |
| chore | config, CI/CD, dependency |

📌 Ví dụ:

```
feat(order): publish OrderCreated event to RabbitMQ
fix(auth): incorrect password hashing logic
docs(global): update DDD architecture diagram
```

---

## 📌 3. Project Structure Rules

### 🏛️ Tổng thể:

```
/project-root
 ├── api-gateway/
 ├── eureka-server/
 ├── service-auth/
 ├── service-menu/
 ├── service-order/
 ├── service-inventory/
 ├── service-payment/
 ├── docker-compose.yml
 └── PROJECT_RULES_AND_STANDARDS.md
```

---

### 🧠 Cấu trúc bắt buộc cho mỗi microservice (DDD style)

```
service-name/
 ├── src/main/java/com/project/[service]/
 │    ├── domain/
 │    │   ├── model/
 │    │   ├── repository/           <-- Interface only
 │    │   ├── event/
 │    │   └── service/              <-- Business logic
 │    │
 │    ├── application/
 │    │   ├── usecase/              <-- Implements user flow
 │    │   ├── dto/
 │    │   └── mapper/
 │    │
 │    ├── infrastructure/
 │    │   ├── repository/           <-- Implementation of domain repository
 │    │   ├── messaging/            <-- Consumers / Producers (RabbitMQ)
 │    │   ├── config/               <-- DB, messaging, external clients
 │    │   └── client/               <-- Feign/API calls to other services
 │    │
 │    └── interfaces/
 │        └── controller/           <-- REST API endpoint
 │
 ├── src/main/resources/application.yml
 └── pom.xml
```

📌 **Controller không được gọi trực tiếp repository.**  
Controller → UseCase → Domain → Repository Interface.

---

## 📌 4. Naming Standards

| Category | Rule | Example |
|----------|------|---------|
| Class | PascalCase | `OrderCreatedEvent` |
| Variable | camelCase | `orderStatus`, `queueName` |
| Enum | PascalCase + UPPER_CASE | `OrderStatus.PENDING` |
| DTO | `[Action][Entity]Dto` | `CreateOrderRequestDto`, `UserResponseDto` |
| Use Case | Verb + Entity + UseCase | `CreateOrderUseCase`, `LoginUseCase` |
| RabbitMQ consumer | `[Event]Listener` | `OrderCreatedEventListener` |

---

## 📌 5. REST API Rules

- RESTful naming
- Không dùng động từ trong URL
- Action dùng HTTP method

| HTTP | URL | Example |
|------|-----|---------|
| GET | /orders | Lấy danh sách |
| POST | /orders | Tạo Order |
| GET | /orders/{id} | Lấy chi tiết |
| PUT | /orders/{id} | Cập nhật |
| DELETE | /orders/{id} | Xóa |

---

## 📌 6. RabbitMQ Messaging Rules

### 🔹 Exchange Naming

```
orders.exchange
payments.exchange
notifications.exchange
```

### 🔹 Queue Naming

```
orders.created.queue
payments.success.queue
inventory.update.queue
```

### 🔹 Routing Key Naming

```
order.created
payment.success
payment.failed
inventory.adjust
```

---

## 📌 7. Development Workflow (Required Order)

> Khi phát triển bất kỳ tính năng nào, phải theo đúng thứ tự:

```
1️⃣ Tạo branch
2️⃣ Viết Use Case (Application layer)
3️⃣ Cập nhật Domain Model (nếu cần)
4️⃣ Tạo Repository Interface (Domain)
5️⃣ Implement Repository (Infrastructure)
6️⃣ Gửi event hoặc xử lý event (nếu có)
7️⃣ Expose Controller (Interface)
8️⃣ Viết test (optional nhưng khuyến nghị)
9️⃣ Tạo Pull Request → Review → Merge
```

---

## 📌 8. Logging Format

All logs must follow:

```
[LEVEL] [SERVICE] [TRACE_ID] ACTION: message
```

Ví dụ:

```
INFO [ORDER-SERVICE] [23af19] EVENT PUBLISHED: order.created ID=102
```

---

## 📌 9. Error Response Standard

Tất cả service phải trả lỗi theo chuẩn JSON:

```json
{
  "timestamp": "2025-01-02T10:12:30Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid email format",
  "path": "/auth/register"
}
```

---

## 📌 10. Documentation Rules

- Tất cả API phải có Swagger UI
- Mỗi service phải có file `README.md` gồm:
  - Chức năng service
  - API Endpoint
  - Sự kiện RabbitMQ sử dụng
  - Môi trường: env, port

---

## 📌 11. Definition of Done ✔

| Requirement | Mandatory |
|------------|-----------|
| Code chạy | ✔ |
| Swagger cập nhật | ✔ |
| RabbitMQ message/event (nếu có) | ✔ |
| Commit đúng format | ✔ |
| Review & merge hợp lệ | ✔ |

---

### 📌 Version

```
Version: v1.1 — DDD Microservices Edition
Updated: 25/11/2025
```

---

**→ Vi phạm các quy tắc trên có thể bị từ chối merge.** 🚨
