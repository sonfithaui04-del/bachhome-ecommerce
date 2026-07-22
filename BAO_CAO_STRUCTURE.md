# 📋 BÁO CÁO ĐỒ ÁN MÔN HỌC
## Hệ Thống Đặt Món Ăn Trực Tuyến - Food Ordering System

**Số trang dự kiến:** 40-50 trang  
**Hình thức:** Theo đồ án tốt nghiệp

---

## TRANG BÌA

**TRƯỜNG ĐẠI HỌC [TÊN TRƯỜNG]**  
**KHOA CÔNG NGHỆ THÔNG TIN**

---

**BÁO CÁO ĐỒ ÁN MÔN HỌC**  
**PHÁT TRIỂN PHẦN MỀM HƯỚNG DỊCH VỤ**

---

**Đề tài:**  
# XÂY DỰNG HỆ THỐNG ĐẶT MÓN ĂN TRỰC TUYẾN VỚI KIẾN TRÚC MICROSERVICES

---

**Sinh viên thực hiện:** Nguyễn Anh Tuấn  
**MSSV:** 2022602738  
**Giảng viên hướng dẫn:** [Tên giảng viên]  
**Năm học:** 2024 - 2025

---

## MỤC LỤC

| Nội dung | Trang |
|----------|-------|
| Danh mục hình ảnh | iii |
| Danh mục bảng | iv |
| Danh mục từ viết tắt | v |
| **PHẦN 1: KIẾN THỨC CƠ SỞ** | |
| Chương 1: Tổng quan kiến trúc phần mềm hướng dịch vụ | 1 |
| Chương 2: Domain-Driven Design (DDD) | 6 |
| Chương 3: API Gateway Pattern | 10 |
| Chương 4: Công nghệ triển khai | 13 |
| **PHẦN 2: GIẢI QUYẾT BÀI TOÁN** | |
| Chương 5: Phân tích yêu cầu | 18 |
| Chương 6: Thiết kế hệ thống với DDD | 23 |
| Chương 7: Thiết kế kiến trúc Microservices | 31 |
| Chương 8: Cài đặt và triển khai | 37 |
| Chương 9: Kết quả và demo | 45 |
| Chương 10: Kết luận và hướng phát triển | 49 |
| Tài liệu tham khảo | 51 |
| Phụ lục | 52 |

---

## DANH MỤC HÌNH ẢNH

| STT | Tên hình | Trang |
|-----|----------|-------|
| 1.1 | So sánh kiến trúc Monolithic và Microservices | 3 |
| 2.1 | Các layer trong DDD | 7 |
| 2.2 | Bounded Context trong hệ thống | 8 |
| 3.1 | Mô hình API Gateway | 10 |
| 3.2 | Service Discovery với Eureka | 12 |
| 4.1 | Docker Container Architecture | 14 |
| 4.2 | Kubernetes Pod và Service | 15 |
| 6.1 | Context Mapping Diagram | 24 |
| 6.2 | ERD - Auth Service | 26 |
| 6.3 | ERD - Menu Service | 27 |
| 6.4 | ERD - Order Service | 28 |
| 7.1 | System Architecture Diagram | 31 |
| 7.2 | Sequence Diagram - Đặt hàng | 35 |
| 8.1 | Docker Compose Deployment | 40 |
| 8.2 | Kubernetes Dashboard | 43 |
| 9.1 | Giao diện đăng nhập | 45 |
| 9.2 | Giao diện danh sách món ăn | 46 |
| 9.3 | Eureka Dashboard | 47 |

---

## DANH MỤC BẢNG

| STT | Tên bảng | Trang |
|-----|----------|-------|
| 1.1 | So sánh SOA và Microservices | 5 |
| 4.1 | Công nghệ sử dụng trong dự án | 17 |
| 5.1 | Danh sách yêu cầu chức năng | 19 |
| 5.2 | Danh sách yêu cầu phi chức năng | 21 |
| 7.1 | Danh sách Microservices | 32 |
| 7.2 | API Endpoints | 34 |
| 8.1 | Cấu hình Docker containers | 41 |
| 8.2 | Kubernetes resources | 44 |

---

## DANH MỤC TỪ VIẾT TẮT

| Từ viết tắt | Ý nghĩa |
|-------------|---------|
| API | Application Programming Interface |
| CI/CD | Continuous Integration / Continuous Deployment |
| DDD | Domain-Driven Design |
| DTO | Data Transfer Object |
| ERD | Entity Relationship Diagram |
| HTTP | HyperText Transfer Protocol |
| JWT | JSON Web Token |
| K8s | Kubernetes |
| MQ | Message Queue |
| REST | Representational State Transfer |
| SOA | Service-Oriented Architecture |
| SQL | Structured Query Language |

---

# PHẦN 1: KIẾN THỨC CƠ SỞ

---

# Chương 1: TỔNG QUAN KIẾN TRÚC PHẦN MỀM HƯỚNG DỊCH VỤ

## 1.1 Giới thiệu về SOA (Service-Oriented Architecture)

### 1.1.1 Khái niệm

**Service-Oriented Architecture (SOA)** là một phong cách thiết kế phần mềm trong đó các thành phần của ứng dụng được chia thành các **dịch vụ (services)** độc lập, có thể tái sử dụng và giao tiếp với nhau thông qua các giao thức chuẩn.

SOA tập trung vào việc:
- Phân tách chức năng thành các dịch vụ riêng biệt
- Xác định rõ ràng interface giữa các dịch vụ
- Cho phép tái sử dụng dịch vụ trong nhiều ngữ cảnh khác nhau

### 1.1.2 Đặc điểm chính

1. **Loose Coupling (Liên kết lỏng lẻo):** Các dịch vụ hoạt động độc lập, thay đổi một dịch vụ không ảnh hưởng đến các dịch vụ khác.

2. **Interoperability (Khả năng tương tác):** Các dịch vụ có thể giao tiếp bất kể ngôn ngữ lập trình hay nền tảng.

3. **Reusability (Tái sử dụng):** Các dịch vụ được thiết kế để có thể sử dụng lại trong nhiều ứng dụng.

4. **Discoverability (Khả năng khám phá):** Các dịch vụ có thể được tìm thấy và truy cập thông qua registry.

### 1.1.3 Ưu nhược điểm

**Ưu điểm:**
- Tăng khả năng tái sử dụng code
- Dễ dàng tích hợp với hệ thống bên ngoài
- Linh hoạt trong việc mở rộng

**Nhược điểm:**
- Phức tạp trong việc triển khai
- Chi phí tích hợp cao (ESB - Enterprise Service Bus)
- Khó khăn trong việc quản lý dependencies

---

## 1.2 Kiến trúc Microservices

### 1.2.1 Khái niệm và nguyên tắc

**Microservices** là một biến thể của SOA, trong đó ứng dụng được cấu trúc thành tập hợp các dịch vụ **nhỏ, độc lập** có thể được phát triển, triển khai và mở rộng riêng biệt.

**Nguyên tắc cốt lõi:**
1. **Single Responsibility:** Mỗi service chỉ thực hiện một chức năng cụ thể
2. **Decentralized Data Management:** Mỗi service có database riêng
3. **API-First Design:** Thiết kế API trước khi implementation
4. **Failure Isolation:** Lỗi của một service không làm sập toàn hệ thống

### 1.2.2 So sánh Microservices vs Monolithic

```
┌─────────────────────────────────────────────────────────────────┐
│                     MONOLITHIC APPLICATION                       │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  User Module    │  Order Module    │  Payment Module     │  │
│  ├──────────────────────────────────────────────────────────┤  │
│  │                    Single Database                        │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                    MICROSERVICES ARCHITECTURE                    │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐                  │
│  │  User    │    │  Order   │    │ Payment  │                  │
│  │ Service  │    │ Service  │    │ Service  │                  │
│  └────┬─────┘    └────┬─────┘    └────┬─────┘                  │
│       │               │               │                         │
│  ┌────┴────┐    ┌────┴────┐    ┌────┴────┐                    │
│  │   DB    │    │   DB    │    │   DB    │                    │
│  └─────────┘    └─────────┘    └─────────┘                    │
└─────────────────────────────────────────────────────────────────┘
```

| Tiêu chí | Monolithic | Microservices |
|----------|------------|---------------|
| Deployment | Toàn bộ ứng dụng | Từng service |
| Scaling | Toàn bộ ứng dụng | Từng service |
| Technology | Một stack | Đa dạng stack |
| Database | Một database chung | Database per service |
| Team | Một team lớn | Nhiều team nhỏ |

### 1.2.3 Ưu nhược điểm

**Ưu điểm:**
- **Độc lập:** Phát triển, deploy, scale riêng từng service
- **Linh hoạt công nghệ:** Mỗi service có thể dùng công nghệ phù hợp
- **Fault Isolation:** Lỗi chỉ ảnh hưởng một service
- **Team nhỏ:** Dễ quản lý, phát triển nhanh

**Nhược điểm:**
- **Phức tạp vận hành:** Cần nhiều công cụ monitoring, logging
- **Network latency:** Giao tiếp qua mạng chậm hơn in-memory
- **Data consistency:** Khó đảm bảo consistent giữa các services
- **Testing phức tạp:** Integration testing khó khăn

### 1.2.4 Các pattern phổ biến trong Microservices

1. **API Gateway Pattern:** Điểm vào duy nhất cho clients
2. **Service Discovery:** Tự động tìm kiếm services
3. **Circuit Breaker:** Xử lý lỗi và ngăn cascading failure
4. **Saga Pattern:** Quản lý distributed transactions
5. **CQRS:** Tách command và query
6. **Event Sourcing:** Lưu trữ trạng thái qua events

---

## 1.3 So sánh SOA và Microservices

| Tiêu chí | SOA | Microservices |
|----------|-----|---------------|
| **Scope** | Enterprise-wide | Application-specific |
| **Communication** | ESB (Enterprise Service Bus) | Lightweight (REST, gRPC) |
| **Data Storage** | Shared database | Database per service |
| **Governance** | Centralized | Decentralized |
| **Service Size** | Large, comprehensive | Small, focused |
| **Reusability** | Maximize reuse | Some duplication OK |

**Khi nào nên dùng Microservices:**
- Ứng dụng phức tạp, cần scale từng phần
- Team lớn, cần phát triển song song
- Yêu cầu continuous deployment
- Cần linh hoạt trong việc chọn công nghệ

---

# Chương 2: DOMAIN-DRIVEN DESIGN (DDD)

## 2.1 Giới thiệu về DDD

### 2.1.1 Khái niệm

**Domain-Driven Design (DDD)** là phương pháp thiết kế phần mềm tập trung vào **domain (lĩnh vực kinh doanh)** và **logic nghiệp vụ**. DDD được giới thiệu bởi Eric Evans trong cuốn sách "Domain-Driven Design: Tackling Complexity in the Heart of Software" (2003).

**Mục tiêu của DDD:**
- Đặt domain và business logic làm trung tâm
- Tạo ngôn ngữ chung giữa developers và domain experts
- Chia hệ thống phức tạp thành các bounded contexts rõ ràng

### 2.1.2 Lịch sử và xu hướng

DDD ra đời để giải quyết vấn đề phần mềm ngày càng phức tạp mà các phương pháp truyền thống không xử lý tốt. Khi kết hợp với Microservices, DDD trở thành công cụ mạnh mẽ để:
- Xác định boundaries cho mỗi microservice
- Định nghĩa rõ ràng trách nhiệm của từng service
- Thiết kế API giao tiếp giữa các services

---

## 2.2 Các khái niệm cốt lõi

### 2.2.1 Ubiquitous Language

**Ubiquitous Language** là ngôn ngữ chung được sử dụng bởi cả developers và domain experts. Ngôn ngữ này được phản ánh trong code, models, và giao tiếp.

**Ví dụ trong hệ thống Food Ordering:**
- `Order` - Đơn hàng
- `MenuItem` - Món ăn trong menu
- `Ingredient` - Nguyên liệu
- `Confirmed`, `Preparing`, `Delivered` - Trạng thái đơn hàng

### 2.2.2 Bounded Context

**Bounded Context** là ranh giới logic trong đó một domain model cụ thể được định nghĩa và áp dụng.

```
┌─────────────────────────────────────────────────────────────┐
│                    FOOD ORDERING SYSTEM                      │
├────────────┬────────────┬────────────┬────────────┬─────────┤
│   Auth     │   Menu     │   Order    │  Payment   │Inventory│
│  Context   │  Context   │  Context   │  Context   │ Context │
├────────────┼────────────┼────────────┼────────────┼─────────┤
│ • User     │ • MenuItem │ • Order    │ • Payment  │• Ingredi│
│ • Role     │ • Category │ • OrderItem│ • Transact │• Recipe │
│ • Login    │ • Variant  │ • Status   │ • Method   │• Stock  │
└────────────┴────────────┴────────────┴────────────┴─────────┘
```

### 2.2.3 Entities và Value Objects

**Entity:** Đối tượng có identity duy nhất, được phân biệt bởi ID.
```java
@Entity
public class Order {
    @Id
    private Long id;  // Identity
    private OrderStatus status;
    private BigDecimal totalAmount;
}
```

**Value Object:** Đối tượng không có identity, được xác định bởi attributes.
```java
public class Address {
    private String street;
    private String city;
    private String zipCode;
}
```

### 2.2.4 Aggregates

**Aggregate** là nhóm các entities và value objects liên quan, được xử lý như một đơn vị. Mỗi aggregate có **Aggregate Root** làm điểm truy cập duy nhất.

```
┌─────────────────────────────────────┐
│         ORDER AGGREGATE             │
│  ┌───────────────────────────────┐ │
│  │     Order (Aggregate Root)    │ │
│  └───────────────┬───────────────┘ │
│                  │                  │
│    ┌─────────────┼─────────────┐   │
│    ▼             ▼             ▼   │
│ OrderItem   OrderItem    DeliveryAddress │
└─────────────────────────────────────┘
```

### 2.2.5 Repositories

**Repository** cung cấp interface để truy cập aggregates, ẩn đi chi tiết persistence.

```java
public interface OrderRepository {
    Order findById(Long id);
    void save(Order order);
    List<Order> findByUserId(Long userId);
}
```

### 2.2.6 Domain Services

**Domain Service** chứa business logic không thuộc về một entity cụ thể.

```java
@Service
public class OrderPricingService {
    public BigDecimal calculateTotal(List<OrderItem> items) {
        return items.stream()
            .map(item -> item.getPrice().multiply(item.getQuantity()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
```

---

## 2.3 Strategic Design vs Tactical Design

### 2.3.1 Strategic Design

Tập trung vào việc chia hệ thống thành các bounded contexts và xác định mối quan hệ giữa chúng:
- **Context Mapping:** Xác định mối quan hệ giữa các contexts
- **Shared Kernel:** Phần chung được chia sẻ giữa các contexts
- **Customer-Supplier:** Một context cung cấp cho context khác

### 2.3.2 Strategic Design

Tập trung vào việc thiết kế chi tiết bên trong mỗi bounded context:
- Entities, Value Objects
- Aggregates, Repositories
- Domain Services, Events

---

## 2.4 Áp dụng DDD trong Microservices

### 2.4.1 DDD Layers Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                     INTERFACES LAYER                          │
│         REST Controllers, DTOs, Validators                    │
├──────────────────────────────────────────────────────────────┤
│                    APPLICATION LAYER                          │
│              Use Cases, Application Services                  │
├──────────────────────────────────────────────────────────────┤
│                      DOMAIN LAYER                             │
│        Entities, Value Objects, Domain Services               │
│              Repository Interfaces                            │
├──────────────────────────────────────────────────────────────┤
│                   INFRASTRUCTURE LAYER                        │
│       Repository Implementations, External APIs               │
│              Database, Messaging                              │
└──────────────────────────────────────────────────────────────┘
```

**Trong dự án Food Ordering:**
```
service-order/
├── interfaces/
│   └── rest/
│       └── OrderController.java
├── application/
│   ├── dto/
│   │   ├── CreateOrderRequest.java
│   │   └── OrderResponse.java
│   └── usecase/
│       └── CreateOrderUseCase.java
├── domain/
│   ├── model/
│   │   ├── Order.java
│   │   └── OrderItem.java
│   ├── repository/
│   │   └── OrderRepository.java
│   └── service/
│       └── OrderDomainService.java
└── infrastructure/
    ├── persistence/
    │   └── JpaOrderRepository.java
    └── messaging/
        └── OrderEventPublisher.java
```

---

# Chương 3: API GATEWAY PATTERN

## 3.1 Giới thiệu API Gateway

### 3.1.1 Khái niệm

**API Gateway** là một server đóng vai trò **điểm vào duy nhất (single entry point)** cho tất cả các requests từ clients đến hệ thống microservices.

```
                    ┌──────────────┐
                    │    Client    │
                    └──────┬───────┘
                           │
                           ▼
                    ┌──────────────┐
                    │ API Gateway  │ ◄── Single Entry Point
                    └──────┬───────┘
                           │
         ┌─────────────────┼─────────────────┐
         ▼                 ▼                 ▼
    ┌─────────┐      ┌─────────┐      ┌─────────┐
    │Service A│      │Service B│      │Service C│
    └─────────┘      └─────────┘      └─────────┘
```

### 3.1.2 Vai trò trong kiến trúc Microservices

- **Simplification:** Client chỉ cần biết 1 endpoint
- **Decoupling:** Client không cần biết internal services
- **Cross-cutting concerns:** Xử lý tập trung authentication, logging, etc.

---

## 3.2 Chức năng của API Gateway

### 3.2.1 Routing

Điều hướng requests đến service phù hợp dựa trên URL pattern:

```yaml
# Ví dụ cấu hình Spring Cloud Gateway
spring:
  cloud:
    gateway:
      routes:
        - id: service-auth
          uri: lb://service-auth
          predicates:
            - Path=/api/auth/**
        - id: service-menu
          uri: lb://service-menu
          predicates:
            - Path=/api/menu/**
        - id: service-order
          uri: lb://service-order
          predicates:
            - Path=/api/orders/**
```

### 3.2.2 Load Balancing

Phân phối traffic đều giữa các instances của service:
- **Round Robin:** Tuần tự qua các instances
- **Weighted:** Theo tỷ lệ được cấu hình
- **Least Connections:** Ưu tiên instance ít kết nối nhất

### 3.2.3 Authentication/Authorization

Xác thực và phân quyền tập trung:
```java
@Bean
public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
    return http
        .authorizeExchange()
            .pathMatchers("/api/auth/**").permitAll()
            .pathMatchers("/api/admin/**").hasRole("ADMIN")
            .anyExchange().authenticated()
        .build();
}
```

### 3.2.4 Rate Limiting

Giới hạn số lượng requests trong một khoảng thời gian:
- Bảo vệ backend services khỏi overload
- Ngăn chặn DDoS attacks

### 3.2.5 Logging & Monitoring

Ghi log tất cả requests/responses để:
- Debug và troubleshooting
- Analytics và metrics
- Security auditing

---

## 3.3 Các giải pháp API Gateway phổ biến

| Giải pháp | Đặc điểm | Use case |
|-----------|----------|----------|
| **Spring Cloud Gateway** | Java-based, reactive, tích hợp Spring | Spring ecosystem |
| **Kong** | Open source, plugin-rich | Enterprise-grade |
| **NGINX** | High performance, reverse proxy | High traffic |
| **AWS API Gateway** | Managed service | AWS cloud |
| **Zuul** | Netflix, non-reactive | Legacy Spring apps |

**Trong dự án này sử dụng Spring Cloud Gateway** vì:
- Tích hợp tốt với Spring Boot
- Reactive, non-blocking I/O
- Dễ cấu hình với Eureka

---

## 3.4 Service Discovery

### 3.4.1 Khái niệm

**Service Discovery** là cơ chế cho phép các services tự động đăng ký và tìm kiếm lẫn nhau mà không cần hardcode addresses.

### 3.4.2 Netflix Eureka

```
┌──────────────────────────────────────────────────────────┐
│                    EUREKA SERVER                          │
│     ┌──────────────────────────────────────────────┐    │
│     │           Service Registry                    │    │
│     │  ┌──────────────────────────────────────┐   │    │
│     │  │ service-auth: 192.168.1.10:8081     │   │    │
│     │  │ service-menu: 192.168.1.11:8082     │   │    │
│     │  │ service-order: 192.168.1.12:8083    │   │    │
│     │  └──────────────────────────────────────┘   │    │
│     └──────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────┘
         ▲                    │
         │ Register           │ Discover
         │                    ▼
    ┌─────────┐          ┌─────────┐
    │ Service │          │ Gateway │
    └─────────┘          └─────────┘
```

**Cấu hình Eureka Client:**
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
```

---

# Chương 4: CÔNG NGHỆ TRIỂN KHAI

## 4.1 Spring Boot

### 4.1.1 Giới thiệu

**Spring Boot** là framework giúp tạo ứng dụng Spring production-ready một cách nhanh chóng với cấu hình tối thiểu.

### 4.1.2 Đặc điểm và ưu điểm

- **Auto-configuration:** Tự động cấu hình dựa trên dependencies
- **Embedded Server:** Tích hợp sẵn Tomcat/Jetty
- **Starter Dependencies:** Quản lý dependencies dễ dàng
- **Production Ready:** Actuator health checks, metrics

### 4.1.3 Spring Cloud Ecosystem

| Component | Chức năng |
|-----------|-----------|
| Spring Cloud Gateway | API Gateway |
| Spring Cloud Netflix Eureka | Service Discovery |
| Spring Cloud Config | Centralized Configuration |
| Spring Cloud Sleuth | Distributed Tracing |
| Spring Cloud Stream | Messaging abstraction |

---

## 4.2 Docker & Container

### 4.2.1 Khái niệm Container

**Container** là đơn vị packaging chứa ứng dụng và tất cả dependencies, chạy cô lập trên host OS.

```
┌─────────────────────────────────────────────────────────┐
│                    HOST OPERATING SYSTEM                 │
├───────────────────────┬─────────────────────────────────┤
│     DOCKER ENGINE     │                                 │
├───────────┬───────────┼───────────────┬─────────────────┤
│Container 1│Container 2│  Container 3  │   Container 4   │
│┌─────────┐│┌─────────┐│ ┌───────────┐ │ ┌─────────────┐ │
││  App    │││  App    ││ │   App     │ │ │    App      │ │
││  Libs   │││  Libs   ││ │   Libs    │ │ │    Libs     │ │
│└─────────┘│└─────────┘│ └───────────┘ │ └─────────────┘ │
└───────────┴───────────┴───────────────┴─────────────────┘
```

### 4.2.2 Dockerfile

```dockerfile
# Multi-stage build
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 4.2.3 Docker Compose

Orchestrate multiple containers:
```yaml
version: '3.8'
services:
  eureka-server:
    build: ./eureka-server
    ports:
      - "8761:8761"
  
  service-auth:
    build: ./service-auth
    depends_on:
      - postgres-auth
      - eureka-server
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres-auth:5432/auth_db
```

---

## 4.3 Kubernetes (K8s)

### 4.3.1 Giới thiệu

**Kubernetes** là platform mã nguồn mở để tự động hóa deployment, scaling, và management của containerized applications.

### 4.3.2 Các thành phần chính

```
┌─────────────────────────────────────────────────────────────┐
│                    KUBERNETES CLUSTER                        │
│  ┌────────────────────────────────────────────────────────┐ │
│  │                    CONTROL PLANE                        │ │
│  │   API Server │ Scheduler │ Controller │ etcd           │ │
│  └────────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────────┐ │
│  │                      WORKER NODE                        │ │
│  │  ┌──────────────────────────────────────────────────┐  │ │
│  │  │  POD         POD          POD                    │  │ │
│  │  │ ┌────────┐  ┌────────┐  ┌────────┐              │  │ │
│  │  │ │Container│  │Container│  │Container│             │  │ │
│  │  │ └────────┘  └────────┘  └────────┘              │  │ │
│  │  └──────────────────────────────────────────────────┘  │ │
│  │  Kubelet │ Kube-proxy │ Container Runtime              │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

**Pod:** Đơn vị nhỏ nhất, chứa 1+ containers
**Service:** Expose pods với stable IP/DNS
**Deployment:** Quản lý replicas và updates
**ConfigMap/Secret:** Quản lý configuration

---

## 4.4 CI/CD Pipeline

### 4.4.1 Khái niệm CI/CD

- **Continuous Integration (CI):** Tự động build và test mỗi khi có code changes
- **Continuous Deployment (CD):** Tự động deploy lên các môi trường

### 4.4.2 GitHub Actions

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
      - name: Build with Maven
        run: mvn clean package
      - name: Build Docker Image
        run: docker build -t my-app .
```

---

## 4.5 Message Queue

### 4.5.1 RabbitMQ

**RabbitMQ** là message broker thực hiện AMQP protocol, cho phép asynchronous communication giữa services.

```
Producer → Exchange → Queue → Consumer

┌─────────────┐    ┌──────────┐    ┌───────────┐    ┌─────────────┐
│Order Service│───▶│ Exchange │───▶│   Queue   │───▶│Payment Svc  │
└─────────────┘    └──────────┘    └───────────┘    └─────────────┘
```

### 4.5.2 Event-Driven Architecture

Các services giao tiếp thông qua events thay vì direct calls:
- **Loose coupling:** Services không cần biết nhau
- **Scalability:** Dễ dàng thêm consumers
- **Resilience:** Queue đóng vai trò buffer

---

# PHẦN 2: GIẢI QUYẾT BÀI TOÁN CỤ THỂ

---

# Chương 5: PHÂN TÍCH YÊU CẦU

## 5.1 Mô tả bài toán

### 5.1.1 Bối cảnh

Trong thời đại số hóa, các nhà hàng/quán ăn cần một hệ thống đặt món ăn trực tuyến để:
- Tiếp cận nhiều khách hàng hơn
- Tối ưu quy trình đặt hàng và vận hành
- Quản lý menu, đơn hàng, thanh toán hiệu quả

### 5.1.2 Mục tiêu

Xây dựng **Hệ thống đặt món ăn trực tuyến** với kiến trúc Microservices, áp dụng:
- Domain-Driven Design (DDD)
- API Gateway Pattern
- Container deployment (Docker, Kubernetes)
- CI/CD automation

---

## 5.2 Yêu cầu chức năng

| STT | Module | Chức năng |
|-----|--------|-----------|
| 1 | **Auth** | Đăng ký, đăng nhập, JWT authentication |
| 2 | **Menu** | CRUD danh mục, món ăn, variants |
| 3 | **Order** | Tạo đơn, cập nhật trạng thái, xem lịch sử |
| 4 | **Payment** | Xử lý thanh toán, ghi nhận transaction |
| 5 | **Inventory** | Quản lý nguyên liệu, công thức, trừ kho |
| 6 | **Notification** | Gửi thông báo qua email/SMS |

---

## 5.3 Yêu cầu phi chức năng

| Yêu cầu | Mô tả |
|---------|-------|
| **Performance** | Response time < 500ms cho 80% requests |
| **Scalability** | Hỗ trợ scale horizontal từng service |
| **Availability** | Uptime > 99.5% |
| **Security** | JWT authentication, HTTPS, input validation |


## 5.4 Các tác nhân (Stakeholders)

| Tác nhân | Vai trò |
|----------|---------|
| **Customer** | Xem menu, đặt hàng, thanh toán |
| **Admin** | Quản lý menu, đơn hàng, users |
| **Staff** | Xử lý đơn hàng, cập nhật trạng thái |

---

# Chương 6: THIẾT KẾ HỆ THỐNG VỚI DDD

## 6.1 Xác định Bounded Contexts

Hệ thống được chia thành **6 Bounded Contexts**, mỗi context tương ứng với 1 microservice:

| Context | Service | Trách nhiệm |
|---------|---------|-------------|
| Auth | service-auth | Quản lý users, authentication |
| Menu | service-menu | Quản lý menu, categories |
| Order | service-order | Quản lý đơn hàng |
| Payment | service-payment | Xử lý thanh toán |
| Inventory | service-inventory | Quản lý kho, nguyên liệu |
| Notification | service-notification | Gửi thông báo |

## 6.2 Context Mapping

```
                    ┌─────────────┐
                    │    Auth     │
                    │   Context   │
                    └──────┬──────┘
                           │ Shared Kernel (User ID)
         ┌─────────────────┼─────────────────┐
         │                 │                 │
         ▼                 ▼                 ▼
   ┌───────────┐    ┌───────────┐    ┌───────────┐
   │   Menu    │    │   Order   │◄───│  Payment  │
   │  Context  │    │  Context  │    │  Context  │
   └─────┬─────┘    └─────┬─────┘    └───────────┘
         │                │
         │    ┌───────────┼───────────┐
         │    │           │           │
         ▼    ▼           ▼           ▼
   ┌───────────┐    ┌───────────────────┐
   │ Inventory │    │   Notification    │
   │  Context  │    │     Context       │
   └───────────┘    └───────────────────┘
```

## 6.3 Domain Model cho từng Service

### 6.3.1 Auth Domain

```java
@Entity
public class User {
    @Id @GeneratedValue
    private Long id;
    private String email;
    private String password;  // BCrypt encoded
    private String fullName;
    private String phoneNumber;
    
    @Enumerated(EnumType.STRING)
    private UserRole role;  // CUSTOMER, ADMIN, STAFF
    
    @Enumerated(EnumType.STRING)
    private UserStatus status;  // ACTIVE, INACTIVE, BANNED
}
```

### 6.3.2 Menu Domain

```java
@Entity
public class MenuItem {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private String imageUrl;
    private Boolean available;
    
    @ManyToOne
    private Category category;
    
    @OneToMany
    private List<MenuItemVariant> variants;
}
```

### 6.3.3 Order Domain (Aggregate)

```java
@Entity
public class Order {  // Aggregate Root
    @Id @GeneratedValue
    private Long id;
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status;  // PENDING, CONFIRMED, PREPARING, DELIVERED, CANCELLED
    
    @OneToMany(cascade = CascadeType.ALL)
    private List<OrderItem> items;
    
    private BigDecimal totalAmount;
    private String deliveryAddress;
    private LocalDateTime createdAt;
    
    // Business methods
    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be confirmed");
        }
        this.status = OrderStatus.CONFIRMED;
    }
}
```

## 6.4 DDD Layers Structure

Mỗi service được tổ chức theo 4 layers:

```
service-order/src/main/java/com/foodordering/order/
├── interfaces/          # Layer 1: Interfaces
│   └── rest/
│       └── OrderController.java
│
├── application/         # Layer 2: Application
│   ├── dto/
│   │   ├── CreateOrderRequest.java
│   │   └── OrderResponse.java
│   └── usecase/
│       ├── CreateOrderUseCase.java
│       └── UpdateOrderStatusUseCase.java
│
├── domain/              # Layer 3: Domain
│   ├── model/
│   │   ├── Order.java
│   │   ├── OrderItem.java
│   │   └── OrderStatus.java
│   ├── repository/
│   │   └── OrderRepository.java
│   └── service/
│       └── OrderDomainService.java
│
└── infrastructure/      # Layer 4: Infrastructure
    ├── persistence/
    │   └── JpaOrderRepository.java
    ├── messaging/
    │   └── OrderEventPublisher.java
    └── config/
        └── RabbitMQConfig.java
```

---

# Chương 7: THIẾT KẾ KIẾN TRÚC MICROSERVICES

## 7.1 Sơ đồ kiến trúc tổng thể

```
┌──────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                        │
│  │ Customer │  │  Admin   │  │  Mobile  │                        │
│  │   Web    │  │  Panel   │  │   App    │                        │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘                        │
└───────┼─────────────┼─────────────┼──────────────────────────────┘
        │             │             │
        └─────────────┼─────────────┘
                      │
               ┌──────▼──────┐
               │ API Gateway │ Port: 8080
               │ (Routing,   │
               │  CORS, Auth)│
               └──────┬──────┘
                      │
         ┌────────────┼────────────┐
         │            │            │
         ▼            ▼            ▼
   ┌──────────┐ ┌──────────┐ ┌──────────┐
   │  Eureka  │ │ RabbitMQ │ │PostgreSQL│
   │  Server  │ │  Broker  │ │ (x6 DBs) │
   │  :8761   │ │  :5672   │ │:5432-5437│
   └──────────┘ └──────────┘ └──────────┘
         ▲            ▲            ▲
         │            │            │
   ┌─────┴────────────┴────────────┴─────┐
   │          MICROSERVICES               │
   │  ┌─────┐ ┌─────┐ ┌─────┐ ┌─────────┐│
   │  │Auth │ │Menu │ │Order│ │ Payment ││
   │  │:8081│ │:8082│ │:8083│ │  :8084  ││
   │  └─────┘ └─────┘ └─────┘ └─────────┘│
   │  ┌───────────┐ ┌─────────────────┐  │
   │  │ Inventory │ │  Notification   │  │
   │  │   :8085   │ │     :8086       │  │
   │  └───────────┘ └─────────────────┘  │
   └─────────────────────────────────────┘
```

## 7.2 Danh sách Services và Ports

| Service | Port | Database | Chức năng |
|---------|------|----------|-----------|
| eureka-server | 8761 | - | Service Discovery |
| api-gateway | 8080 | - | Routing, CORS |
| service-auth | 8081 | postgres-auth:5432 | Authentication |
| service-menu | 8082 | postgres-menu:5433 | Menu management |
| service-order | 8083 | postgres-order:5434 | Order management |
| service-payment | 8084 | postgres-payment:5435 | Payment processing |
| service-inventory | 8085 | postgres-inventory:5436 | Stock management |
| service-notification | 8086 | postgres-notification:5437 | Notifications |

## 7.3 API Gateway Configuration

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: service-auth
          uri: lb://service-auth
          predicates:
            - Path=/api/auth/**, /api/users/**
          filters:
            - StripPrefix=1
            
        - id: service-menu
          uri: lb://service-menu
          predicates:
            - Path=/api/menu/**, /api/categories/**
          filters:
            - StripPrefix=1
            
        - id: service-order
          uri: lb://service-order
          predicates:
            - Path=/api/orders/**
          filters:
            - StripPrefix=1
```

## 7.4 Inter-service Communication

### Synchronous (REST)
```
Client → API Gateway → Service A → Response
```

### Asynchronous (RabbitMQ)
```
Order Service → order.created event → Exchange → Queue → Payment Service
                                                      → Inventory Service
                                                      → Notification Service
```

## 7.5 Sequence Diagram - Đặt hàng

```
┌────────┐     ┌─────────┐     ┌───────────┐     ┌─────────┐     ┌───────────┐
│ Client │     │ Gateway │     │  Order    │     │ Payment │     │ Inventory │
└───┬────┘     └────┬────┘     └─────┬─────┘     └────┬────┘     └─────┬─────┘
    │               │                │                │                │
    │ POST /orders  │                │                │                │
    │──────────────▶│                │                │                │
    │               │ forward        │                │                │
    │               │───────────────▶│                │                │
    │               │                │ create order   │                │
    │               │                │────────────────│                │
    │               │                │                │                │
    │               │                │ event: order.created            │
    │               │                │───────────────────────────────▶│
    │               │                │────────────────▶│               │
    │               │                │ [async]        │ deduct stock  │
    │               │                │                │               │
    │               │◀───────────────│                │                │
    │◀──────────────│  Order Created │                │                │
    │               │                │                │                │
```

---

# Chương 8: CÀI ĐẶT VÀ TRIỂN KHAI

## 8.1 Môi trường phát triển

| Công nghệ | Phiên bản | Mục đích |
|-----------|-----------|----------|
| Java | 17 LTS | Runtime |
| Spring Boot | 3.2.1 | Framework |
| Spring Cloud | 2023.0.0 | Microservices tools |
| PostgreSQL | 15 | Database |
| RabbitMQ | 3-management | Message broker |
| Docker | 24.x | Containerization |
| Kubernetes | 1.28 | Container orchestration |

## 8.2 Cấu trúc thư mục dự án

```
food-ordering/
├── eureka-server/          # Service Discovery
├── api-gateway/            # API Gateway
├── service-auth/           # Auth Microservice
├── service-menu/           # Menu Microservice
├── service-order/          # Order Microservice
├── service-payment/        # Payment Microservice
├── service-inventory/      # Inventory Microservice
├── service-notification/   # Notification Microservice
├── frontend/               # Customer Web App
├── frontend-admin/         # Admin Panel
├── k8s/                    # Kubernetes manifests
│   ├── namespace.yaml
│   ├── configmap.yaml
│   ├── deployments.yaml
│   ├── databases.yaml
│   └── ingress.yaml
├── .github/
│   └── workflows/
│       └── ci-cd.yml       # GitHub Actions
└── docker-compose.yml      # Docker Compose
```

## 8.3 Docker Compose Configuration

```yaml
version: '3.8'

services:
  eureka-server:
    build: ./eureka-server
    ports:
      - "8761:8761"
    networks:
      - food-ordering-network

  api-gateway:
    build: ./api-gateway
    ports:
      - "8080:8080"
    environment:
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka/
    depends_on:
      - eureka-server

  service-auth:
    build: ./service-auth
    ports:
      - "8081:8081"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres-auth:5432/food_ordering_auth
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka/
    depends_on:
      - postgres-auth
      - eureka-server

  postgres-auth:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: food_ordering_auth
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    volumes:
      - postgres-auth-data:/var/lib/postgresql/data

networks:
  food-ordering-network:
    driver: bridge

volumes:
  postgres-auth-data:
```

## 8.4 Kubernetes Deployment

### Deployment YAML
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: service-auth
  namespace: food-ordering
spec:
  replicas: 2
  selector:
    matchLabels:
      app: service-auth
  template:
    metadata:
      labels:
        app: service-auth
    spec:
      containers:
        - name: service-auth
          image: food-ordering-service-auth:latest
          imagePullPolicy: Never
          ports:
            - containerPort: 8081
          env:
            - name: SPRING_DATASOURCE_URL
              value: "jdbc:postgresql://postgres-auth:5432/food_ordering_auth"
          resources:
            requests:
              memory: "256Mi"
              cpu: "100m"
            limits:
              memory: "512Mi"
              cpu: "500m"
```

## 8.5 CI/CD Pipeline

```yaml
name: Food Ordering CI/CD

on:
  push:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          
      - name: Build All Services
        run: |
          cd service-auth && mvn clean package -DskipTests
          cd ../service-menu && mvn clean package -DskipTests
          cd ../service-order && mvn clean package -DskipTests
          
      - name: Build Docker Images
        run: docker-compose build
        
      - name: Push to Registry
        run: docker-compose push
```

---

# Chương 9: KẾT QUẢ VÀ DEMO

*(Nội dung chương này đã được chuyển sang file riêng để tiện theo dõi và trích xuất báo cáo. Vui lòng xem file `CHUONG_9_KET_QUA_DEMO.md`)*

---
---

# Chương 10: KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN

## 10.1 Kết luận

Đồ án đã hoàn thành việc xây dựng **Hệ thống đặt món ăn trực tuyến** với các mục tiêu:

✅ **Domain-Driven Design:** Áp dụng DDD với 4 layers cho từng service  
✅ **API Gateway:** Triển khai Spring Cloud Gateway với routing, CORS  
✅ **Microservices:** 7 services độc lập với database-per-service  
✅ **Docker/Docker Compose:** 15+ containers orchestrated  
✅ **Kubernetes:** Deploy thành công với 19 pods  
✅ **CI/CD:** GitHub Actions pipeline

## 10.2 Hạn chế

- Chưa có unit tests đầy đủ
- Payment gateway chưa tích hợp thực
- Notification chưa gửi email/SMS thật
- Chưa có distributed tracing

## 10.3 Hướng phát triển

1. **Thêm Redis caching** để cải thiện performance
2. **Distributed Tracing** với Zipkin/Jaeger
3. **ELK Stack** cho centralized logging
4. **Real payment gateway** (VNPay, Momo)
5. **Mobile app** (React Native/Flutter)
6. **Kubernetes trên cloud** (GKE, EKS, AKS)

---

# TÀI LIỆU THAM KHẢO

1. Eric Evans - "Domain-Driven Design: Tackling Complexity in the Heart of Software" (2003)
2. Vaughn Vernon - "Domain-Driven Design Distilled" (2016)
3. Sam Newman - "Building Microservices" (2021)
4. Chris Richardson - "Microservices Patterns" (2018)
5. Spring Boot Documentation - https://docs.spring.io/spring-boot
6. Docker Documentation - https://docs.docker.com
7. Kubernetes Documentation - https://kubernetes.io/docs
8. RabbitMQ Documentation - https://www.rabbitmq.com/documentation.html

---

# PHỤ LỤC

## Phụ lục A: Danh sách API Endpoints

| Method | Endpoint | Service | Mô tả |
|--------|----------|---------|-------|
| POST | /api/auth/register | Auth | Đăng ký user |
| POST | /api/auth/login | Auth | Đăng nhập |
| GET | /api/menu | Menu | Lấy danh sách món |
| POST | /api/orders | Order | Tạo đơn hàng |
| PATCH | /api/orders/{id}/status | Order | Cập nhật trạng thái |
| POST | /api/payments | Payment | Thanh toán |

## Phụ lục B: Cấu hình Eureka Server

```yaml
server:
  port: 8761

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
  server:
    enable-self-preservation: false
```

## Phụ lục C: Docker commands

```bash
# Build all services
docker-compose build

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f service-auth

# Stop all
docker-compose down
```

## Phụ lục D: Kubernetes commands

```bash
# Apply all manifests
kubectl apply -f k8s/

# View pods
kubectl get pods -n food-ordering

# View logs
kubectl logs -f <pod-name> -n food-ordering
```

---

**Ngày hoàn thành:** Tháng 12/2025
