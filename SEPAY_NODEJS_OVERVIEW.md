# 💳 SePay Integration - React + Node.js + MongoDB

> **Tổng quan nhanh** để tích hợp thanh toán SePay vào dự án React + Node.js + MongoDB

---

## Kiến Trúc Tổng Quan

```
┌─────────────────────────────────────────────────────────────────┐
│                     FRONTEND (React + Vite)                      │
│  ┌──────────────────┐    ┌──────────────────────────────────┐   │
│  │   PaymentModal   │    │     Socket.io Client             │   │
│  │   (hiển thị QR)  │    │  (nhận PAYMENT_SUCCESS realtime) │   │
│  └──────────────────┘    └──────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     BACKEND (Node.js + Express)                  │
│                                                                  │
│  ┌──────────────────┐    ┌──────────────────┐    ┌───────────┐  │
│  │ POST /payment/   │    │ POST /payment/   │    │ Socket.io │  │
│  │   sepay/init     │    │   sepay/webhook  │    │  Server   │  │
│  │ (tạo QR code)    │    │ (SePay callback) │    │           │  │
│  └──────────────────┘    └──────────────────┘    └───────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     DATABASE (MongoDB)                           │
│  ┌──────────┐    ┌──────────┐    ┌──────────────────────────┐   │
│  │  Orders  │    │ Payments │    │   paymentStatus field    │   │
│  └──────────┘    └──────────┘    │  (pending/success/failed)│   │
│                                  └──────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

---

## Luồng Hoạt Động

```
1. User checkout → Frontend gọi POST /payment/sepay/init
                              ↓
2. Backend tạo QR URL (VietQR) → Trả về {qrCodeUrl, bankInfo, description}
                              ↓
3. Frontend hiển thị QR popup → User quét & chuyển khoản
                              ↓
4. SePay nhận giao dịch → Gọi webhook POST /payment/sepay/webhook
                              ↓
5. Backend parse nội dung CK ("DH{orderId}") → Update DB
                              ↓
6. Backend emit Socket.io event → Frontend nhận → Hiển thị Success Modal
```

---

## Các File/Module Cần Tạo

### Backend (Node.js + Express)

```
/server
├── models/
│   ├── Order.js           # Schema: thêm paymentStatus, paymentMethod
│   └── Payment.js         # Schema mới: orderId, amount, status, transactionId
├── routes/
│   └── payment.routes.js  # POST /init, POST /webhook
├── services/
│   └── sepay.service.js   # createQRUrl(), parseWebhook()
├── config/
│   └── sepay.config.js    # bankCode, accountNumber, accountName
└── socket.js              # Socket.io server setup
```

### Frontend (React)

```
/client
├── components/
│   └── PaymentModal.jsx   # Hiển thị QR + thông tin thanh toán
├── services/
│   ├── paymentService.js  # API calls: initPayment()
│   └── socketService.js   # Socket.io client
└── hooks/
    └── usePaymentSocket.js # Hook nhận PAYMENT_SUCCESS event
```

---

## Mối Nối Chính (Integration Points)

### 1️⃣ Backend → SePay (QR Generation)

```javascript
// sepay.service.js
const createQRUrl = (orderId, amount) => {
  const description = `DH${orderId}`;
  const qrUrl = `https://qr.sepay.vn/img?acc=${ACCOUNT_NUMBER}&bank=${BANK_CODE}&amount=${amount}&des=${description}&template=compact`;
  return { qrUrl, description, bankCode, accountNumber, accountName };
};
```

### 2️⃣ SePay → Backend (Webhook)

```javascript
// POST /payment/sepay/webhook (KHÔNG cần auth)
router.post('/sepay/webhook', async (req, res) => {
  const { content, transferAmount, transactionId } = req.body;
  
  // Parse: content = "DH123" → orderId = 123
  const orderId = content.match(/DH(\d+)/)?.[1];
  
  // Update Payment & Order
  await Payment.updateOne({ orderId }, { status: 'success', transactionId });
  await Order.updateOne({ _id: orderId }, { paymentStatus: 'success' });
  
  // Emit Socket.io
  io.to(`user_${userId}`).emit('PAYMENT_SUCCESS', { orderId });
  
  res.json({ status: 'success' });
});
```

### 3️⃣ Backend → Frontend (Realtime via Socket.io)

```javascript
// Backend: socket.js
io.on('connection', (socket) => {
  socket.on('join', (userId) => socket.join(`user_${userId}`));
});

// Khi webhook được gọi:
io.to(`user_${userId}`).emit('PAYMENT_SUCCESS', { orderId, message: '...' });
```

```javascript
// Frontend: usePaymentSocket.js
useEffect(() => {
  socket.emit('join', userId);
  socket.on('PAYMENT_SUCCESS', (data) => {
    setPaymentSuccess(true);
    // Hiển thị modal, reload orders, etc.
  });
  return () => socket.off('PAYMENT_SUCCESS');
}, [userId]);
```

---

## Cấu Hình SePay

```javascript
// config/sepay.config.js
module.exports = {
  BANK_CODE: 'MB',           // Mã ngân hàng (MB, VCB, TCB, ...)
  ACCOUNT_NUMBER: '123456789',
  ACCOUNT_NAME: 'NGUYEN VAN A'
};
```

**Đăng ký SePay:**
1. Vào [sepay.vn](https://sepay.vn) → Đăng ký
2. Thêm tài khoản ngân hàng → Xác thực
3. Vào Webhook → Nhập URL: `https://your-domain.com/api/payment/sepay/webhook`

---

## Khi Bạn Đưa Project

Tôi sẽ cần xem:
1. **Cấu trúc thư mục** của project
2. **Order model** hiện tại
3. **Checkout flow** hiện tại (API endpoint nào?)
4. **Socket.io** đã setup chưa?

Sau đó tôi sẽ:
- Tạo Payment model (MongoDB)
- Tạo payment routes
- Tạo SePay service
- Tạo PaymentModal component
- Kết nối Socket.io cho realtime notification

---

> 📌 **Ghi nhớ**: Nội dung chuyển khoản phải format chuẩn `DH{orderId}` để webhook có thể parse được.
