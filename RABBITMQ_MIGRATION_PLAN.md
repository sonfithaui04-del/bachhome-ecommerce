# Kế hoạch Chuyển đổi sang RabbitMQ (Event-Driven Architecture)

Chúng ta sẽ chuyển đổi luồng xử lý đơn hàng từ đồng bộ (HTTP) sang bất đồng bộ (RabbitMQ).

## ✅ Bước 1: Hạ tầng & Cấu hình (Đã hoàn thành)
- [x] Thêm RabbitMQ vào `docker-compose.yml`.
- [x] Thêm `spring-boot-starter-amqp` vào `service-inventory` và `service-notification`.

## ✅ Bước 2: Cấu hình RabbitMQ trong Code (Đã hoàn thành)
- [x] Tạo `RabbitMQConfig` trong `service-order` (Producer).
- [x] Tạo `RabbitMQConfig` trong `service-inventory` (Consumer).
- [x] Tạo `RabbitMQConfig` trong `service-notification` (Consumer).
- [x] Định nghĩa các Queue và Exchange.

## ✅ Bước 3: Refactor Logic Nghiệp vụ (Đã hoàn thành)
- [x] **Service Order:**
    - Sửa `OrderController`: Khi confirm đơn hàng -> Bắn sự kiện `OrderConfirmedEvent`.
    - Xóa code gọi trực tiếp `InventoryServiceClient`.
- [x] **Service Inventory:**
    - Tạo `OrderEventListener`: Lắng nghe `order.inventory.queue`.
    - Xử lý trừ kho khi nhận message.
- [x] **Service Notification:**
    - Tạo `NotificationListener`: Lắng nghe `order.notification.queue`.
    - Log thông báo gửi email.

## ⏳ Bước 4: Kiểm thử & Mở rộng
- [x] Restart toàn bộ hệ thống (`start-all.bat` hoặc `docker-compose up -d --build`).
- [x] Đặt hàng và kiểm tra log.
- [ ] (Future) Xử lý thất bại (Compensating Transaction / Saga) nếu trừ kho lỗi.
