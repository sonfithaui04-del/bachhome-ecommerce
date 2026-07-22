# Menu Service

## 📋 Mô tả
Service quản lý sản phẩm và danh mục cho hệ thống BachHome.

## 🔧 Công nghệ
- Spring Boot 3.2.1
- Spring Data JPA
- PostgreSQL
- Netflix Eureka Client
- Swagger/OpenAPI 3
- Java 17

## 🏗️ Kiến trúc DDD

```
service-menu/
├── domain/              # Business Logic Layer
│   ├── model/          # Entities (Category, MenuItem)
│   └── repository/     # Repository Interfaces
├── application/         # Use Cases Layer
│   ├── usecase/        # Business use cases
│   └── dto/            # Data Transfer Objects
├── infrastructure/      # Technical Layer
│   ├── repository/     # JPA Implementations
│   └── config/         # Configurations
└── interfaces/          # API Layer
    └── controller/     # REST Controllers
```

## 🚀 Cách chạy

### Prerequisites
- Java 17
- PostgreSQL
- Maven

### 1. Cấu hình Database
```sql
CREATE DATABASE food_ordering_menu;
```

### 2. Chạy Service
```bash
mvn spring-boot:run
```

## 🌐 API Endpoints

### Categories

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/categories` | Lấy tất cả categories |
| GET | `/categories?activeOnly=true` | Lấy active categories |
| POST | `/categories` | Tạo category mới |

### Menu Items

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/menu` | Lấy tất cả sản phẩm |
| GET | `/menu?availableOnly=true` | Lấy sản phẩm đang bán |
| POST | `/menu` | Tạo sản phẩm mới |

## 📊 Swagger UI
http://localhost:8082/swagger-ui.html

## 📝 Example Requests

### Create Category
```bash
curl -X POST http://localhost:8082/categories \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Nhà bếp",
    "description": "Đồ gia dụng nhà bếp",
    "displayOrder": 1
  }'
```

### Create Menu Item
```bash
curl -X POST http://localhost:8082/menu \
  -H "Content-Type: application/json" \
  -d '{
    "categoryId": 1,
    "name": "Nồi cơm điện",
    "description": "Nồi cơm điện 1.8L chống dính cao cấp",
    "price": 890000,
    "imageUrl": "https://example.com/noi-com-dien.jpg"
  }'
```

### Get All Products
```bash
curl http://localhost:8082/menu
```

## ⚙️ Configuration

- **Port**: 8082
- **Database**: PostgreSQL (localhost:5433)
- **Eureka Server**: http://localhost:8761/eureka/
- **Service Name**: service-menu

## 📌 Database Schema

### categories
- id (PK)
- name
- description
- display_order
- active
- created_at
- updated_at

### menu_items
- id (PK)
- category_id (FK)
- name
- description
- price
- image_url
- available
- display_order
- created_at
- updated_at

## 🔗 Dependencies
- Eureka Server (localhost:8761)
- PostgreSQL (localhost:5433)
