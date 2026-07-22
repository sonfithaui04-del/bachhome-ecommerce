# 📚 Hướng Dẫn Sử Dụng & Giải Thích Chi Tiết Hệ Thống Food Ordering

Tài liệu này được biên soạn chi tiết để giải thích toàn bộ kiến thức kỹ thuật, luồng dữ liệu và phân chia trách nhiệm trong nhóm.

## 👥 Phân Chia Trách Nhiệm

## 👥 Phân Chia Trách Nhiệm

### 1. Đỗ Thiên Nhật (Luồng Chính & Nâng Cao - System Architect)
Chịu trách nhiệm toàn bộ các tính năng cốt lõi và kỹ thuật phức tạp của hệ thống:
- **Core Backend**:
  - **Authentication**: Đăng ký, Đăng nhập, Bảo mật (JWT), phân quyền User/Admin.
  - **Order & Menu Services**: Xử lý logic đặt hàng, quản lý món ăn, tồn kho.
- **Advanced Integrations (Nâng cao)**:
  - **Thanh toán Online (SePay)**: Tích hợp cổng thanh toán, xử lý Webhook, bảo mật giao dịch.
  - **Event-Driven Architecture**: Cấu hình **RabbitMQ** để xử lý giao tiếp bất đồng bộ giữa các services (Payment -> Order -> Inventory).
  - **Realtime System**: Xây dựng hệ thống Notification thời gian thực với **WebSocket (STOMP)**.
- **DevOps**: Cấu hình Docker, Docker Compose, triển khai hệ thống.

### 2. Nguyễn Xuân Lập (Luồng Phụ & Frontend Support)
Chịu trách nhiệm các tính năng bổ trợ và giao diện người dùng cơ bản:
- **Frontend UI/UX**:
  - Xây dựng giao diện Landing Page, Navbar, Footer.
  - Thiết kế Responsive (Mobile/Desktop).
- **Client-Side Logic**:
  - **Cart Management**: Quản lý trạng thái giỏ hàng (thêm/sửa/xóa) dùng Context API.
  - **User Profile**: Trang xem và cập nhật thông tin cá nhân, lịch sử đơn hàng (phần hiển thị).
- **Testing & Documentation**: Kiểm thử cơ bản các luồng, viết tài liệu hướng dẫn.

---

## 🏗️ Tổng Quan Kiến Trúc & Lý Thuyết

Hệ thống sử dụng kiến trúc **Microservices**. Thay vì một "cục" code khổng lồ (Monolith), chúng ta chia nhỏ thành các services chạy riêng biệt.

```
Frontend (React) ◄──HTTP──► API Gateway (8080) ◄──HTTP──► Services (Auth, Menu, Order...)
                                                                  ▲
                                                                  │ (Async Messaging)
                                                                  ▼
                                                               RabbitMQ
```

**Tại sao lại dùng kiến trúc này?**
- **Dễ chia việc**: Nhật làm Auth/Menu không ảnh hưởng code Payment của Lập.
- **Dễ mở rộng**: Nếu tính năng Payment quá tải, chỉ cần chạy thêm service Payment mà không cần chạy lại cả hệ thống.
- **Khả năng chịu lỗi**: Nếu service Notification chết, người dùng vẫn đặt hàng được (chỉ là không thấy thông báo ngay thôi).

---

## 📘 1. Phần Của Đỗ Thiên Nhật (Core & Advanced Backend)

Là người chịu trách nhiệm chính về kỹ thuật (System Architect), bạn nắm toàn bộ backend và các luồng dữ liệu phức tạp.

### 🔐 1.1 Luồng Authentication (System Security)
**Luồng đi:** `Request` -> `Gateway` -> `Service-Auth` -> `PostgreSQL`

**Kiến thức cần nắm (Lý thuyết):**
- **JWT (JSON Web Token)**: Là "thẻ bài" user cầm sau khi login. Service không lưu Session (Stateless), mỗi request user gửi kèm token này để server biết họ là ai.
- **BCrypt Hashing**: Mật khẩu không lưu text trơn mà được băm (hash) thành chuỗi ký tự ngẫu nhiên. Dù hacker lấy được DB cũng không biết pass gốc.

**Chi Tiết Code:**
```java
// service-auth/.../JwtService.java
public String generateToken(User user)
// - Mục đích: Tạo ra chuỗi JWT khi user login thành công.
// - Logic: 
//   1. Lấy thông tin User (ID, Email, Roles).
//   2. Set thời gian hết hạn (expiration).
//   3. Ký (Sign) bằng thuật toán HS256 với Secret Key.
// - Output: Chuỗi "ey...xyz" gửi về cho Client.

public boolean validateToken(String token)
// - Mục đích: Middleware kiểm tra token có hợp lệ không.
// - Logic: Giải mã token bằng Secret Key. Nếu giải được và chưa hết hạn -> True.
```

### 🍔 1.2 Luồng Order & Menu (Core Business)
**Luồng đi:** `Frontend` -> `API Gateway` -> `Service-Order` -> `RabbitMQ`

**Kiến thức cần nắm (Lý thuyết):**
- **DTO (Data Transfer Object)**: Object hứng dữ liệu từ Frontend, giúp lọc dữ liệu an toàn trước khi map vào Entity.
- **@Transactional**: Đảm bảo toàn vẹn dữ liệu. Nếu lưu OrderItems lỗi thì Order cũng tự hủy, tránh đơn hàng rác.

**Chi Tiết Code:**
```java
// service-order/.../CreateOrderUseCase.java
@Transactional
public OrderDto execute(CreateOrderDto request)
// - Mục đích: Logic nghiệp vụ tạo đơn hàng.
// - Logic:
//   1. Validate: Kiểm tra tồn kho, tính lại tổng tiền từ DB (không tin giá từ client).
//   2. Save Order: Lưu thông tin chung (User, Tổng tiền, Trạng thái PENDING).
//   3. Save Items: Lưu chi tiết từng món ăn.
//   4. Publish Event: Gửi tin "ORDER_CREATED" vào RabbitMQ.
// - Rollback: Nếu bước 3 lỗi, bước 2 tự hủy.
```

### 💳 1.3 Luồng Thanh Toán Online (Advanced Integration)
**Luồng đi:** `Frontend` -> `Service-Payment` -> `SePay Webhook` -> `Service-Payment` -> `RabbitMQ`

**Kiến thức cần nắm (Lý thuyết):**
- **VietQR**: Chuẩn tạo QR code ngân hàng Việt Nam. Chỉ cần đúng format URL là app ngân hàng quét được.
- **Webhook**: Cơ chế "gõ cửa báo tin". SePay tự gọi API của ta khi có tiền về, thay vì ta phải đi hỏi (polling).
- **Idempotency**: Đảm bảo xử lý trùng lặp. Nếu SePay lỡ gọi webhook 2 lần cho 1 đơn, ta chỉ xử lý lần đầu.

**Chi Tiết Code:**
```java
// service-payment/.../SePayService.java
public SePayPaymentInfo createPaymentInfo(Long orderId, BigDecimal amount)
// - Mục đích: Tạo URL QR Code động.
// - Logic: String.format("https://qr.sepay.vn/img?acc=%s&bank=%s&amount=%s&des=DH%s", ...)
// - Quan trọng: 'des' (nội dung) phải là "DH" + OrderID để máy đọc được.

// service-payment/.../PaymentController.java
public ResponseEntity sePayWebhook(SePayWebhookRequest webhook)
// - Mục đích: API nhận báo cáo từ SePay.
// - Logic:
//   1. Parse nội dung CK (VD: "DH105" -> OrderID 105).
//   2. Check duplicate: Tìm xem đơn 105 đã thanh toán chưa.
//   3. Update DB: PaymentStatus = SUCCESS.
//   4. Publish Event: Gửi tin nhắn Async cho Order Service biết.
```

### 🐇 1.4 Luồng Bất Đồng Bộ (RabbitMQ)
**Vấn đề**: Tại sao Webhook không gọi thẳng Order Service update?
**Giải pháp**: Giảm sự phụ thuộc (Decoupling). Nếu Order Service đang bảo trì, Payment Service vẫn nhận tiền bình thường, tin nhắn được lưu trong Queue chờ xử lý sau.

**Chi Tiết Code:**
```java
// service-order/.../PaymentEventListener.java
@RabbitListener(queues = RabbitMQConfig.PAYMENT_CONFIRMED_QUEUE)
public void handlePaymentConfirmed(Map<String, Object> event)
// - Mục đích: Consumer lắng nghe tin "Tiền đã về".
// - Logic:
//   1. Lấy OrderID từ message.
//   2. Tìm Order trong DB.
//   3. Set paymentStatus = "PAID".
//   4. Save Order.
//   5. Fire & Forget: Bắn tiếp event "NOTIFY_USER" sang Socket Service.
```

### 🔔 1.5 Realtime Updates (WebSocket)
**Kiến thức cần nắm (Lý thuyết):**
- **WebSocket vs HTTP**: HTTP là "hỏi-đáp" (Client hỏi mới trả lời). WebSocket là "ống nước" (Server chủ động bơm tin xuống).
- **STOMP Protocol**: Giao thức định nghĩa cách chat trong ống nước (subscribe/send).

**Chi Tiết Code:**
```java
// service-socket/.../SocketEventListener.java
@RabbitListener(...)
public void handleEvent(Object event)
// - Mục đích: Cầu nối giữa RabbitMQ (Backend) và WebSocket (Frontend).
// - Logic:
//   1. Nhận tin từ các service khác (Order/Payment).
//   2. Xác định người nhận (UserID).
//   3. Push tin xuống topic: "/topic/user/" + userId.
```

---

## 📙 2. Phần Của Nguyễn Xuân Lập (Frontend & Client-Side Logic)

Là người phụ trách trải nghiệm người dùng, bạn đảm bảo giao diện phản hồi mượt mà và gọi API chính xác.

### 🛒 2.1 Quản Lý State (Cart Context)
**Vấn đề**: Giỏ hàng cần truy cập được từ trang Menu (để thêm) và trang Checkout (để thanh toán).
**Giải pháp**: Dùng **Context API** bao bọc toàn bộ ứng dụng.

**Chi Tiết Code:**
```javascript
// frontend/src/context/CartContext.jsx
const addToCart = (product) => {
  // - Mục đích: Thêm món vào giỏ hàng local.
  // - Logic:
  //   1. Check: `cart.find(item => item.id === product.id)`
  //   2. If exist: `item.quantity += 1`
  //   3. If not exist: `cart.push({...product, quantity: 1})`
  //   4. Sync: `localStorage.setItem('cart', ...)` để giữ lại khi F5.
}

const totalAmount = useMemo(() => {
  // - Mục đích: Tính tổng tiền hiệu năng cao (chỉ tính lại khi cart đổi).
  // - Logic: cart.reduce((total, item) => total + item.price * item.quantity, 0)
}, [cart])
```

### 🎨 2.2 Giao Diện & Trải Nghiệm (UI/UX)
**Kiến thức**:
- **TailwindCSS**: Utility-first framework giúp CSS nhanh ngay trong HTML class.
- **Component Lifecycle (useEffect)**: Quản lý vòng đời component (sinh ra -> update -> chết đi).

**Chi Tiết Code:**
```javascript
// frontend/.../Navbar.jsx
useEffect(() => {
  // - Mục đích: Đăng ký lắng nghe sự kiện khi Navbar được vẽ ra.
  window.addEventListener('notification_received', handleNotification)
  // - Cleanup: Hủy lắng nghe khi Navbar biến mất (tránh memory leak).
  return () => window.removeEventListener(...)
}, [])

// frontend/.../MyOrdersPage.jsx
const getStatusBadge = (status) => {
  // - Mục đích: Helper function chọn màu sắc status.
  // - Logic:
  //   case 'PENDING': return 'bg-yellow-100 text-yellow-800'
  //   case 'COMPLETED': return 'bg-green-100 text-green-800'
}
```

### 🔄 2.3 Tương Tác API (Axios Integration)
**Kiến thức**: Code frontend gọi xuống server của Nhật. Xử lý bất đồng bộ (Async/Await) để giao diện không bị đơ khi chờ server trả lời.

**Chi Tiết Code:**
```javascript
// frontend/src/pages/CheckoutPage.jsx
const handleSubmit = async () => {
  try {
    // 1. Loading State: setSubmitting(true) để disable nút bấm.
    // 2. API Call: 
    const res = await orderService.createOrder(data);
    
    // 3. Success Handling: Check payment method.
    if (data.paymentMethod === 'SEPAY') {
       // Mở modal QR code
       loadPaymentInfo(res.orderId);
    } else {
       // Chuyển trang thành công
       navigate('/success');
    }
  } catch (err) {
    // 4. Error Handling: Hiện thông báo lỗi đẹp từ server.
    toast.error(err.response?.data?.message || 'Có lỗi xảy ra');
  }
}
```

---

## 🛠️ Tóm Tắt Cách Phối Hợp

**Kịch bản: User A vào mua hàng và thanh toán Online**

1. **(Lập)** User bấm "Thêm vào giỏ" -> Code `CartContext` của Lập chạy, lưu vào RAM.
2. **(Lập)** User bấm "Checkout" -> Lập gọi API `POST /orders` của Nhật.
3. **(Nhật)** Backend nhận đơn -> Lưu trạng thái PENDING -> Trả về OrderID.
4. **(Lập)** Thấy đơn tạo thành công -> Gọi tiếp API `POST /payment/sepay/init` của Nhật để lấy ảnh QR.
5. **(Nhật)** Backend tạo URL QR -> Trả về cho Lập.
6. **(Lập)** Hiển thị Popup chứa QR Code.
7. **(System)** User quét mã -> Webhook gọi về Service của Nhật -> Nhật xử lý qua RabbitMQ -> Socket.
8. **(Lập)** Socket Client nhận tín hiệu "PAYMENT_SUCCESS" -> Tự động hiện thông báo "Thanh toán thành công" và đóng Popup.

---

> 💡 **Lời khuyên đi thi/báo cáo**:
> - Khi thầy hỏi **"Tại sao đơn hàng đang chờ thanh toán mà Service Order lại biết update?"** -> Trả lời ngay: **"Dùng cơ chế Event-Driven qua RabbitMQ. Payment Service bắn sự kiện, Order Service lắng nghe và tự cập nhật."**
> - Khi thầy hỏi **"Làm sao đảm bảo user A không nhận nhầm notif của user B?"** -> Trả lời: **"Dùng WebSocket với topic riêng biệt `/topic/user/{userId}`. Chỉ client subscribe đúng topic userId của mình mới nhận được tin."**
