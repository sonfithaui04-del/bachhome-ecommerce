# Các Mẫu Test API (Postman Samples) cho Báo cáo

Dưới đây là bảng chi tiết các Test Case API điển hình được sử dụng để kiểm thử tích hợp hệ thống qua API Gateway. Bạn có thể sao chép các bảng này trực tiếp vào báo cáo.

## 1. Xác thực (Authentication)

| Test Case | API Login (Đăng nhập) |
| :--- | :--- |
| **Mục đích** | Lấy JWT Token để truy cập các API bảo mật. |
| **Method** | `POST` |
| **URL** | `http://localhost:8080/api/auth/login` |
| **Body (JSON)** | ```json<br>{<br>  "username": "nguyenvanan",<br>  "password": "password"<br>}<br>``` |
| **Expected Result** | **HTTP 200 OK** |
| **Response** | ```json<br>{<br>  "token": "eyJhbGciOiJIUzI1NiIsIn...",<br>  "type": "Bearer",<br>  "id": 1,<br>  "username": "nguyenvanan",<br>  "roles": ["ROLE_USER"]<br>}<br>``` |

---

## 2. Quản lý Đơn hàng (Order Service)

### A. Tạo Đơn hàng Mới

| Test Case | API Create Order (Tạo đơn) |
| :--- | :--- |
| **Mục đích** | Tạo một đơn hàng mới với danh sách món ăn. |
| **Method** | `POST` |
| **URL** | `http://localhost:8080/api/orders` |
| **Headers** | `Authorization: Bearer <jwt_token>` |
| **Body (JSON)** | ```json<br>{<br>  "items": [<br>    { "productId": 1, "quantity": 2 }<br>  ],<br>  "paymentMethod": "COD",<br>  "address": "123 Main St, Hanoi"<br>}<br>``` |
| **Expected Result** | **HTTP 200 OK** |
| **Response** | ```json<br>{<br>  "id": 105,<br>  "userId": 1,<br>  "status": "PENDING",<br>  "totalAmount": 150000,<br>  "items": [...] <br>}<br>``` |

### B. Hủy Đơn hàng (Cập nhật trạng thái)

| Test Case | API Cancel Order (Hủy đơn) |
| :--- | :--- |
| **Mục đích** | Người dùng hoặc Admin hủy đơn hàng khi trạng thái là PENDING. |
| **Method** | `PATCH` |
| **URL** | `http://localhost:8080/api/orders/{orderId}/status` |
| **Headers** | `Authorization: Bearer <jwt_token>` |
| **Body (JSON)** | ```json<br>{<br>  "status": "CANCELLED"<br>}<br>``` |
| **Expected Result** | **HTTP 200 OK** |
| **Response** | ```json<br>{<br>  "id": 105,<br>  "status": "CANCELLED",<br>  "updatedAt": "2025-12-21T15:00:00"<br>}<br>``` |

---

## 3. Menu Service

| Test Case | API Get Menu (Xem thực đơn) |
| :--- | :--- |
| **Mục đích** | Lấy danh sách món ăn hiển thị cho khách hàng (Public API). |
| **Method** | `GET` |
| **URL** | `http://localhost:8080/api/menu` |
| **Headers** | _None_ |
| **Body** | _None_ |
| **Expected Result** | **HTTP 200 OK** |
| **Response** | ```json<br>[<br>  { "id": 1, "name": "Phở Bò", "price": 50000 },<br>  { "id": 2, "name": "Bún Chả", "price": 60000 }<br>]<br>``` |
