# 🏗️ Architecture Overview - Food Ordering System

## 📊 System Architecture Diagram

```
┌──────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐         │
│  │   Web    │  │  Mobile  │  │  Admin   │  │   API    │         │
│  │   App    │  │   App    │  │  Panel   │  │  Client  │         │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘         │
└───────┼────────────┼─────────────┼─────────────┼────────────────┘
        │            │             │             │
        └────────────┴─────────────┴─────────────┘
                          │
                          ▼
┌──────────────────────────────────────────────────────────────────┐
│                      API GATEWAY (Port 8080)                      │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  • Routing              • Rate Limiting                  │    │
│  │  • Load Balancing       • CORS Configuration             │    │
│  │  • Authentication       • Request/Response Logging       │    │
│  └─────────────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌──────────────────────────────────────────────────────────────────┐
│              EUREKA SERVER - Service Discovery (8761)             │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  • Service Registry                                      │    │
│  │  • Health Monitoring                                     │    │
│  │  • Service Discovery                                     │    │
│  └─────────────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────────────┘
                          │
        ┌─────────────────┼─────────────────┐
        │                 │                 │
        ▼                 ▼                 ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│    Auth      │  │    Menu      │  │    Order     │
│  Service     │  │   Service    │  │   Service    │
│  (8081)      │  │   (8082)     │  │   (8083)     │
├──────────────┤  ├──────────────┤  ├──────────────┤
│ • Register   │  │ • Items CRUD │  │ • Create     │
│ • Login      │  │ • Categories │  │ • Update     │
│ • JWT Token  │  │ • Search     │  │ • Status     │
│ • Validate   │  │ • Pricing    │  │ • History    │
└──────┬───────┘  └──────┬───────┘  └──────┬───────┘
       │                 │                 │
       ▼                 ▼                 ▼
  ┌─────────┐      ┌─────────┐       ┌─────────┐
  │   DB    │      │   DB    │       │   DB    │
  │  5432   │      │  5433   │       │  5434   │
  └─────────┘      └─────────┘       └─────────┘

        ┌─────────────────┼─────────────────┐
        │                 │                 │
        ▼                 ▼                 ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│  Inventory   │  │   Payment    │  │Notification  │
│   Service    │  │   Service    │  │   Service    │
│   (8084)     │  │   (8085)     │  │   (8086)     │
├──────────────┤  ├──────────────┤  ├──────────────┤
│ • Stock      │  │ • Process    │  │ • Email      │
│ • Update     │  │ • Validate   │  │ • SMS        │
│ • Check      │  │ • Refund     │  │ • Push       │
│ • Reserve    │  │ • History    │  │ • Templates  │
└──────┬───────┘  └──────┬───────┘  └──────┬───────┘
       │                 │                 │
       ▼                 ▼                 ▼
  ┌─────────┐      ┌─────────┐       (External)
  │   DB    │      │   DB    │       Email/SMS API
  │  5435   │      │  5436   │
  └─────────┘      └─────────┘
```

## 🔄 Event Flow with RabbitMQ

```
┌──────────────────────────────────────────────────────────────────┐
│                    RABBITMQ MESSAGE BROKER (5672)                 │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                     EXCHANGES                            │    │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐              │    │
│  │  │  orders  │  │ payments │  │inventory │              │    │
│  │  │ exchange │  │ exchange │  │ exchange │              │    │
│  │  └────┬─────┘  └────┬─────┘  └────┬─────┘              │    │
│  └───────┼─────────────┼─────────────┼────────────────────┘    │
└──────────┼─────────────┼─────────────┼───────────────────────────┘
           │             │             │
    ┌──────┴──────┬──────┴──────┬──────┴──────┐
    │             │             │             │
    ▼             ▼             ▼             ▼
┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐
│order.   │  │payment. │  │inventory│  │notify.  │
│created  │  │success  │  │.update  │  │order    │
│.queue   │  │.queue   │  │.queue   │  │.queue   │
└────┬────┘  └────┬────┘  └────┬────┘  └────┬────┘
     │            │            │            │
     ▼            ▼            ▼            ▼
  [Payment]   [Inventory]  [Order]    [Notification]
  [Service]   [Service]    [Service]   [Service]
```

### Event Examples:

1. **Order Created Flow:**
   ```
   Order Service → order.created → [Payment, Inventory, Notification]
   ```

2. **Payment Success Flow:**
   ```
   Payment Service → payment.success → [Order, Notification]
   ```

3. **Inventory Updated Flow:**
   ```
   Inventory Service → inventory.updated → [Menu, Order]
   ```

## 🗂️ DDD Layer Architecture (Per Service)

```
┌──────────────────────────────────────────────────────────────────┐
│                         INTERFACES LAYER                          │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │           REST Controllers (API Endpoints)               │    │
│  │  • Validation    • Swagger Docs    • Exception Handlers │    │
│  └───────────────────────┬─────────────────────────────────┘    │
└──────────────────────────┼───────────────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────────────┐
│                       APPLICATION LAYER                           │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    USE CASES                             │    │
│  │  • RegisterUseCase      • CreateOrderUseCase             │    │
│  │  • LoginUseCase         • UpdateOrderUseCase             │    │
│  │  • ProcessPaymentUseCase                                 │    │
│  └───────────────────────┬─────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                      DTOs                                │    │
│  │  • Request DTOs         • Response DTOs                  │    │
│  └───────────────────────┬─────────────────────────────────┘    │
└──────────────────────────┼───────────────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────────────┐
│                         DOMAIN LAYER                              │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    DOMAIN MODELS                         │    │
│  │  • Entities         • Value Objects    • Enums           │    │
│  │  • Aggregates       • Business Rules                     │    │
│  └─────────────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │              REPOSITORY INTERFACES                       │    │
│  │  (Contracts - No Implementation)                         │    │
│  └───────────────────────┬─────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                 DOMAIN SERVICES                          │    │
│  │  • Complex Business Logic                                │    │
│  └───────────────────────┬─────────────────────────────────┘    │
└──────────────────────────┼───────────────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────────────┐
│                    INFRASTRUCTURE LAYER                           │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │           REPOSITORY IMPLEMENTATIONS                     │    │
│  │  • JPA Repositories     • Adapters                       │    │
│  └─────────────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                   MESSAGING                              │    │
│  │  • RabbitMQ Producers   • RabbitMQ Consumers             │    │
│  └─────────────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │              CONFIGURATIONS                              │    │
│  │  • Security    • Database    • External APIs             │    │
│  └─────────────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────────────┘
```

## 🔒 Security Architecture

```
     ┌──────────┐
     │  Client  │
     └────┬─────┘
          │ (1) Request without token
          ▼
  ┌──────────────┐
  │ API Gateway  │ (2) Route to Auth Service
  └──────┬───────┘
         │
         ▼
  ┌──────────────┐
  │ Auth Service │ (3) Validate credentials
  └──────┬───────┘      Generate JWT Token
         │
         │ (4) Return JWT Token
         ▼
     ┌──────────┐
     │  Client  │ Store token
     └────┬─────┘
          │ (5) Request with Bearer Token
          ▼
  ┌──────────────┐
  │ API Gateway  │ (6) Validate token (optional)
  └──────┬───────┘      Route to service
         │
         ▼
  ┌──────────────┐
  │Any Service   │ (7) Process request
  └──────────────┘      (Trust API Gateway)
```

## 📊 Database Schema Overview

### Auth Service Database

```
users
├── id (PK)
├── email (UNIQUE)
├── password (hashed)
├── full_name
├── phone_number
├── role (ENUM: CUSTOMER, ADMIN, STAFF)
├── status (ENUM: ACTIVE, INACTIVE, BANNED)
├── created_at
└── updated_at
```

### Menu Service Database (Planned)

```
categories                  menu_items
├── id (PK)                ├── id (PK)
├── name                   ├── category_id (FK)
├── description            ├── name
└── display_order          ├── description
                           ├── price
                           ├── image_url
                           ├── is_available
                           └── created_at
```

### Order Service Database (Planned)

```
orders                      order_items
├── id (PK)                ├── id (PK)
├── user_id                ├── order_id (FK)
├── total_amount           ├── menu_item_id
├── status                 ├── quantity
├── created_at             ├── price
└── updated_at             └── subtotal
```

## 🔄 API Request Flow Example

### Example: Create Order

```
1. Client → POST /api/orders/create
           Headers: Authorization: Bearer <token>
           Body: { items: [...] }
           │
           ▼
2. API Gateway (Port 8080)
   • Validate request
   • Route to Order Service
           │
           ▼
3. Order Service (Port 8083)
   • Extract user from JWT token
   • CreateOrderUseCase.execute()
   • Save order to database
   • Publish "order.created" event to RabbitMQ
           │
           ├──────────────────────┬──────────────────┐
           ▼                      ▼                  ▼
4a. Payment Service      4b. Inventory Service   4c. Notification
    Listen order.created     Listen order.created    Listen order.created
    Process payment          Reserve stock           Send email
    Publish payment.success  Update inventory        Send SMS
           │
           ▼
5. Order Service
   Listen payment.success
   Update order status
   Return response to client
```

## 📈 Scalability & High Availability

```
        Load Balancer
              │
    ┌─────────┼─────────┐
    ▼         ▼         ▼
┌─────────┐ ┌─────────┐ ┌─────────┐
│Gateway 1│ │Gateway 2│ │Gateway 3│
└────┬────┘ └────┬────┘ └────┬────┘
     │           │           │
     └───────────┼───────────┘
                 │
    ┌────────────┼────────────┐
    ▼            ▼            ▼
┌─────────┐  ┌─────────┐  ┌─────────┐
│Order    │  │Order    │  │Order    │
│Service 1│  │Service 2│  │Service 3│
└─────────┘  └─────────┘  └─────────┘
     │            │            │
     └────────────┼────────────┘
                  ▼
         Database Cluster
       (Master-Slave Replication)
```

## 🎯 Technology Stack Summary

| Layer | Technology |
|-------|-----------|
| **API Gateway** | Spring Cloud Gateway |
| **Service Discovery** | Netflix Eureka |
| **Backend Framework** | Spring Boot 3.2.1 |
| **Messaging** | RabbitMQ |
| **Database** | PostgreSQL 15 |
| **Authentication** | JWT (JJWT 0.12.3) |
| **Security** | Spring Security |
| **ORM** | Spring Data JPA / Hibernate |
| **Documentation** | Swagger/OpenAPI 3 |
| **Containerization** | Docker & Docker Compose |
| **Build Tool** | Maven |
| **Java Version** | Java 17 |

---

**Last Updated**: 26/11/2025  
**Version**: 1.0.0
