# Đọc hiểu hệ thống BachHome

Tài liệu này dành cho người **mới tiếp cận mã nguồn** và cần hiểu hệ thống đủ sâu để sửa code
hoặc trả lời câu hỏi khi bảo vệ. Đọc theo thứ tự từ trên xuống, mất khoảng **45–60 phút**.

> Nếu chỉ cần chạy được hệ thống thì đọc [README.md](README.md) là đủ, không cần file này.

---

## 1. Bức tranh trong một phút

BachHome là website bán **đồ gia dụng**, không viết thành một khối duy nhất mà tách thành
**8 microservice** chạy độc lập, mỗi service có **database riêng**, giao tiếp với nhau bằng
**REST** (khi cần trả lời ngay) và **RabbitMQ** (khi không cần chờ).

Trình duyệt **không bao giờ gọi thẳng** vào service nào. Mọi request đi qua **API Gateway**,
gateway hỏi **Eureka** xem service đang nằm ở đâu rồi mới chuyển tiếp.

```
Trình duyệt ──► API Gateway (8080) ──hỏi Eureka (8761)──► service tương ứng
                                    └─ mỗi service ─────► PostgreSQL riêng của nó
                                    └─ sự kiện ─────────► RabbitMQ ──► service khác nghe
```

---

## 2. Ba câu hỏi cốt lõi

### 2.1. Tại sao phải chia nhỏ ra nhiều service?

Ba lý do thực dụng, nêu đúng ba cái này là đủ:

1. **Mở rộng riêng lẻ.** Ngày sale, lượng người *xem sản phẩm* gấp nhiều lần lượng người
   *thanh toán*. Kiến trúc này cho phép nhân bản riêng `service-product` mà không đụng
   `service-payment`.
2. **Hỏng cục bộ.** `service-notification` chết thì khách vẫn đặt hàng được, chỉ là không
   nhận được thông báo. Nếu là một khối duy nhất thì chết là chết cả.
3. **Triển khai độc lập.** Sửa logic tồn kho chỉ cần build lại `service-inventory`, không
   phải deploy lại toàn hệ thống.

**Cái giá phải trả** (nên chủ động nói ra, đừng để bị hỏi ngược): phức tạp hơn hẳn — phải có
service discovery, gateway, message broker; dữ liệu nằm rải rác nên **không JOIN được giữa
các service**; và phải chấp nhận *nhất quán cuối cùng* thay vì transaction ACID xuyên suốt.

### 2.2. Client gọi vào đâu?

Chỉ một cửa: **API Gateway cổng 8080**. Cấu hình route nằm ở
[api-gateway/src/main/resources/application.yml](api-gateway/src/main/resources/application.yml):

| Đường dẫn | Chuyển tới |
|---|---|
| `/api/auth/**`, `/api/users/**` | SERVICE-AUTH |
| `/api/products/**`, `/api/categories/**` | SERVICE-PRODUCT |
| `/api/orders/**` | SERVICE-ORDER |
| `/api/payments/**` | SERVICE-PAYMENT |
| `/api/inventory/**`, `/api/inventory-items/**` | SERVICE-INVENTORY |
| `/api/notifications/**`, `/api/chat/**` | SERVICE-NOTIFICATION |
| `/api/ai/**` | SERVICE-AI |
| `/ws/**` | SERVICE-SOCKET |

Hai điểm hay bị hỏi:

- **`lb://SERVICE-PRODUCT` nghĩa là gì?** `lb` = load balanced. Gateway không biết IP của
  service, nó hỏi Eureka "SERVICE-PRODUCT đang ở đâu", Eureka trả về danh sách instance,
  gateway chọn một cái theo vòng tròn. Chạy 3 bản `service-product` thì tự động chia tải.
- **`StripPrefix=1` để làm gì?** Cắt bỏ đoạn `/api` trước khi chuyển tiếp. Client gọi
  `/api/products`, service thực tế nhận `/products`.

### 2.3. Các service nói chuyện với nhau kiểu gì?

Có **hai kiểu**, dùng đúng chỗ:

| | Đồng bộ (REST) | Bất đồng bộ (RabbitMQ) |
|---|---|---|
| Khi nào | Cần câu trả lời ngay mới đi tiếp được | Bắn xong đi luôn, không chờ |
| Ví dụ | Tạo đơn → hỏi tồn kho còn đủ không | Xác nhận đơn → báo trừ kho, gửi email |
| Hỏng thì sao | Request lỗi theo | Message nằm trong queue chờ service sống lại |
| Code | [InventoryServiceClient.java](service-order/src/main/java/com/bachhome/order/infrastructure/client/InventoryServiceClient.java) | `rabbitTemplate.convertAndSend(...)` |

---

## 3. Bản đồ 8 service

| Service | Cổng | Database | Giữ dữ liệu gì |
|---|---|---|---|
| service-auth | 8081 | bachhome_auth | `users` (email, mật khẩu băm, role) |
| service-product | 8082 | bachhome_product | `products`, `categories`, `reviews` |
| service-order | 8083 | bachhome_order | `orders`, `order_items` |
| service-payment | 8084 | bachhome_payment | giao dịch thanh toán |
| service-inventory | 8085 | bachhome_inventory | `inventory_items` (tồn kho) |
| service-notification | 8086 | bachhome_notification | thông báo đã gửi |
| service-ai | 8087 | *không có* | chatbot, gọi Gemini |
| service-socket | 8089 | *không có* | đẩy realtime qua WebSocket |

Cộng thêm **eureka-server (8761)** làm sổ địa chỉ và **api-gateway (8080)** làm cửa vào.

Chú ý hai service **không có database**: `service-ai` chỉ trung chuyển sang Gemini,
`service-socket` chỉ đẩy message ra trình duyệt.

---

## 4. Kể lại một đơn hàng từ đầu đến cuối

Đây là phần quan trọng nhất. Hiểu được luồng này là hiểu được cả hệ thống.

### Bước 1 — Khách bấm "Đặt hàng"

Trình duyệt gọi `POST /api/orders` → Gateway → `service-order`.

`CreateOrderUseCase` chạy:
1. Với **từng sản phẩm** trong giỏ, gọi **đồng bộ** sang `service-inventory` hỏi còn hàng
   không ([CreateOrderUseCase.java:55](service-order/src/main/java/com/bachhome/order/application/usecase/CreateOrderUseCase.java#L55)).
   Không đủ → ném lỗi, đơn không được tạo.
2. Đủ hàng → lưu đơn vào `bachhome_order` với trạng thái **PENDING**.

> **Vì sao gọi đồng bộ ở đây?** Vì phải biết kết quả *ngay* mới quyết định được có tạo đơn
> hay không. Không thể "bắn message rồi tính sau".

### Bước 2 — Admin xác nhận đơn

`PATCH /api/orders/{id}/status` với `status=CONFIRMED`.

`service-order` **không tự trừ kho**. Nó bắn một sự kiện lên RabbitMQ rồi trả lời client ngay:

```java
rabbitTemplate.convertAndSend("bachhome-exchange", "order.confirmed", event);
```

### Bước 3 — Ba service cùng nghe một sự kiện

Đây là điểm hay nhất của kiến trúc này. Một sự kiện `order.confirmed` được **ba** service
nhận độc lập, không service nào biết service kia tồn tại:

| Queue | Service nghe | Làm gì |
|---|---|---|
| `order.inventory.queue` | service-inventory | Trừ số lượng tồn kho |
| `order.notification.queue` | service-notification | Gửi email cho khách |
| `socket-notification-queue` | service-socket | Đẩy thông báo realtime |

Muốn thêm chức năng "ghi log thống kê khi đơn được xác nhận"? Chỉ cần viết service mới, tạo
queue mới bind vào routing key `order.confirmed`. **Không phải sửa một dòng nào** trong
`service-order`. Đó chính là ý nghĩa của "tách rời" (loose coupling).

### Bước 4 — Thông báo hiện lên màn hình khách

`service-socket` nhận message từ queue rồi đẩy xuống trình duyệt qua WebSocket:

- `/topic/user/{userId}` — thông báo riêng cho một khách
- `/topic/admin/notifications` — thông báo cho tất cả admin

Trình duyệt đã kết nối sẵn từ trước qua SockJS/STOMP
([socketService.js](frontend/src/services/socketService.js)), nên chuông báo hiện lên
**không cần F5**.

### Bước 5 — Thanh toán QR (nếu chọn SePay)

1. Khách chọn chuyển khoản → `POST /api/payments/sepay/init` sinh mã QR.
2. Khách quét, chuyển tiền.
3. **SePay gọi ngược về hệ thống**: `POST /api/payments/sepay/webhook`.
4. `service-payment` bắn sự kiện `payment.confirmed` lên RabbitMQ.
5. `service-order` nghe queue `payment.confirmed.order-service`, cập nhật đơn thành đã thanh toán.

> Webhook là **bên ngoài gọi vào mình**, ngược với REST thông thường. Vì SePay không thể biết
> khi nào khách chuyển tiền xong nên phải để nó chủ động báo.

### Sơ đồ tổng hợp RabbitMQ

Tất cả đi qua một exchange kiểu **topic** tên `bachhome-exchange`:

```
order.confirmed ──┬──► order.inventory.queue      ──► service-inventory (trừ kho)
                  ├──► order.notification.queue   ──► service-notification (email)
                  └──► socket-notification-queue  ──► service-socket (realtime)

order.status.changed ─► socket-notification-queue ──► service-socket (realtime)

payment.confirmed ───► payment.confirmed.order-service ──► service-order (đánh dấu đã trả tiền)
```

Xem tận mắt tại http://localhost:15672 (RabbitMQ Management, guest/guest) → tab **Queues**.

---

## 5. Đọc code theo tầng DDD

Mỗi service có đúng 4 thư mục, và **luồng gọi luôn đi một chiều**:

```
interfaces/  →  application/  →  domain/  ←  infrastructure/
(controller)    (use case)       (model)      (JPA, RabbitMQ)
```

| Tầng | Chứa gì | Quy tắc |
|---|---|---|
| `interfaces/` | REST controller | Chỉ nhận request, gọi use case, trả response. **Không chứa logic nghiệp vụ** |
| `application/` | Use case + DTO | Điều phối các bước của một nghiệp vụ |
| `domain/` | Entity, repository *interface* | Trái tim hệ thống. **Không phụ thuộc Spring/JPA** |
| `infrastructure/` | JPA impl, RabbitMQ, config | Chi tiết kỹ thuật, cắm vào domain |

**Vì sao domain không được biết gì về database?** Để đổi từ PostgreSQL sang MongoDB thì chỉ
sửa `infrastructure/`, logic nghiệp vụ giữ nguyên. Đó là mục đích của DDD.

### Nên mở file nào trước

Đọc `service-product` trước vì nó đơn giản nhất, đủ 4 tầng, không có message queue:

1. [ProductController.java](service-product/src/main/java/com/bachhome/product/interfaces/controller/ProductController.java) — điểm vào
2. [GetAllProductsUseCase.java](service-product/src/main/java/com/bachhome/product/application/usecase/GetAllProductsUseCase.java) — use case
3. [Product.java](service-product/src/main/java/com/bachhome/product/domain/model/Product.java) — entity
4. [ProductRepository.java](service-product/src/main/java/com/bachhome/product/domain/repository/ProductRepository.java) — interface ở domain
5. [ProductRepositoryImpl.java](service-product/src/main/java/com/bachhome/product/infrastructure/repository/ProductRepositoryImpl.java) — implement ở infrastructure

Hiểu xong 5 file này thì **mọi service khác đều cùng một khuôn**.

Sau đó đọc `service-order` để thấy thêm phần bắn sự kiện, và `service-inventory` để thấy
phần nghe sự kiện.

---

## 6. Chatbot AI hoạt động ra sao

`service-ai` (8087) không tự "biết" sản phẩm. Quy trình mỗi lần khách hỏi:

1. [ProductContextProvider](service-ai/src/main/java/com/bachhome/ai/infrastructure/ProductContextProvider.java)
   gọi `http://SERVICE-PRODUCT/products?availableOnly=true` qua Eureka, lấy tối đa 40 sản phẩm.
2. [ChatService](service-ai/src/main/java/com/bachhome/ai/application/ChatService.java) nhét
   danh sách đó vào **system prompt**, kèm ràng buộc *"chỉ được gợi ý sản phẩm trong danh sách
   này, không bịa thêm"*.
3. Gọi Google Gemini, nhận câu trả lời.
4. `matchSuggestions()` dò xem tên sản phẩm nào xuất hiện trong câu trả lời (chuẩn hoá: bỏ dấu
   câu, gộp khoảng trắng, chuyển chữ thường) để trả kèm **ảnh và giá** cho frontend hiển thị
   thẻ sản phẩm.

**Câu hỏi hay gặp: "Làm sao chống AI bịa sản phẩm không có thật?"** — Bằng bước 2: prompt chỉ
chứa hàng đang bán và có ràng buộc rõ ràng; cộng thêm bước 4 chỉ hiển thị thẻ cho sản phẩm
khớp đúng tên trong database.

Chi tiết vận hành xem [CHATBOT-AI-README.md](CHATBOT-AI-README.md).

---

## 7. Mô hình tồn kho

`inventory_items` giữ số lượng tồn. Mỗi bản ghi có cột **`product_id`** trỏ tới sản phẩm đang
bán ở `service-product`.

- `product_id` **có giá trị** → là hàng bán trực tiếp, bán ra thì trừ kho tương ứng.
- `product_id` **null** → là vật tư kho (thùng carton, băng keo…), không gắn với sản phẩm nào.

**Vì sao không dùng khoá ngoại?** Vì `products` và `inventory_items` nằm ở **hai database khác
nhau** — nguyên tắc *database per service*. Không thể tạo FOREIGN KEY xuyên database, nên chỉ
lưu ID và tra cứu qua API khi cần.

Luồng trừ kho: `service-inventory` nghe `order.confirmed` →
[OrderEventListener](service-inventory/src/main/java/com/bachhome/inventory/infrastructure/messaging/OrderEventListener.java)
tìm `inventory_item` theo `productId` → trừ đúng số lượng đã đặt.

---

## 8. Bảo mật

- Đăng nhập thành công → `service-auth` phát **JWT** chứa userId và role, ký bằng HS512.
- Frontend lưu token, gắn vào header `Authorization: Bearer <token>` cho mọi request sau.
- Mật khẩu lưu dạng **băm BCrypt**, không bao giờ lưu thô.
- Role: `CUSTOMER`, `ADMIN`, `STAFF`, `SHIPPER`. Admin Panel yêu cầu role `ADMIN`.

**Vì sao dùng JWT thay vì session?** Vì có nhiều service. Session phải lưu trạng thái tập
trung, mọi service phải hỏi chung một chỗ. JWT thì **tự chứa thông tin**, service nào cũng
tự xác thực được bằng khoá bí mật chung, không cần gọi ngược về `service-auth`.

---

## 9. Tự kiểm chứng (chạy thử để tin)

```bash
# Xem 9 service đã đăng ký Eureka chưa
curl http://localhost:8761/eureka/apps -H "Accept: application/json"

# Lấy danh sách sản phẩm (đi qua gateway, gateway hỏi Eureka, Eureka chỉ tới service-product)
curl "http://localhost:8080/api/products?availableOnly=true"

# Xem tồn kho
curl http://localhost:8080/api/inventory-items

# Đăng nhập lấy JWT
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"admin@bachhome.com\",\"password\":\"admin123\"}"

# Hỏi chatbot
curl -X POST http://localhost:8080/api/ai/chat \
  -H "Content-Type: application/json" \
  -d "{\"message\":\"Tư vấn nồi cơm điện\"}"

# Xem log một service để thấy nó nhận sự kiện
docker compose logs -f service-inventory
```

**Bài tập hiểu bài:** mở 2 cửa sổ — một cửa sổ chạy `docker compose logs -f service-inventory`,
cửa sổ kia đổi trạng thái một đơn hàng sang `CONFIRMED` trên Admin Panel. Anh sẽ thấy log
`[INVENTORY-LISTENER] Received OrderConfirmedEvent` xuất hiện ngay. Đó là RabbitMQ đang chạy
trước mắt mình.

---

## 10. Những điểm yếu đã biết

Nói ra trước còn hơn bị hỏi vặn. Đây là các hạn chế **có thật** trong mã nguồn:

1. **Saga pattern chưa hoàn chỉnh.** Nếu trừ kho thất bại sau khi đơn đã CONFIRMED, đơn vẫn ở
   trạng thái CONFIRMED — chưa có cơ chế rollback. Trong
   [OrderEventListener](service-inventory/src/main/java/com/bachhome/inventory/infrastructure/messaging/OrderEventListener.java)
   còn ghi `// TODO: Gửi sự kiện InventoryFailedEvent`. Đây là **hệ quả tất yếu của việc bỏ
   transaction ACID** khi tách database.

2. **Hoàn kho khi huỷ đơn chưa bật.** Ở `OrderController`, nhánh `CONFIRMED → CANCELLED` đang
   để dòng `// restoreInventoryForOrder(order);` bị comment. Hai phương thức
   `deductInventoryForOrder` và `restoreInventoryForOrder` hiện **không được gọi** — việc trừ
   kho đã chuyển hẳn sang cơ chế bất đồng bộ qua RabbitMQ.

3. **SePay dùng thông tin giả.** Trong `docker-compose.yml`, ba biến `SEPAY_*` vẫn là
   `YOUR_ACCOUNT_NUMBER`. Demo được luồng COD, còn QR cần tài khoản SePay thật.

4. **Chưa có kiểm thử tự động.** Không có unit test / integration test nào.

5. **Chatbot phụ thuộc quota miễn phí** của Google Gemini. Hết quota thì trả câu xin lỗi
   thay vì tư vấn.

---

## 11. Đọc tiếp gì

| File | Khi nào cần |
|---|---|
| [ARCHITECTURE.md](ARCHITECTURE.md) | Sơ đồ chi tiết hơn, schema từng database |
| [PROJECT_RULES_AND_STANDARDS.md](PROJECT_RULES_AND_STANDARDS.md) | Trước khi viết code mới |
| [CHATBOT-AI-README.md](CHATBOT-AI-README.md) | Khi làm việc với chatbot |
| [MO_TA_CHI_TIET_CHUC_NANG.md](MO_TA_CHI_TIET_CHUC_NANG.md) | Khi viết báo cáo — mô tả từng use case |
| [SEPAY_INTEGRATION_GUIDE.md](SEPAY_INTEGRATION_GUIDE.md) | Khi đụng vào thanh toán QR |
