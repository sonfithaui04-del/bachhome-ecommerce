# 💳 Hướng Dẫn Tích Hợp Thanh Toán SePay

> **Mục đích**: Tài liệu này hướng dẫn cách tích hợp thanh toán online qua SePay vào bất kỳ hệ thống web nào.

---

## Mục Lục
1. [Giới Thiệu SePay](#giới-thiệu-sepay)
2. [Đăng Ký Tài Khoản SePay](#đăng-ký-tài-khoản-sepay)
3. [Kiến Trúc Tích Hợp](#kiến-trúc-tích-hợp)
4. [Backend Implementation (Spring Boot)](#backend-implementation-spring-boot)
5. [Frontend Implementation (React)](#frontend-implementation-react)
6. [Xử Lý Webhook](#xử-lý-webhook)
7. [Realtime Notification](#realtime-notification)
8. [Testing với ngrok](#testing-với-ngrok)
9. [Checklist Triển Khai](#checklist-triển-khai)

---

## Giới Thiệu SePay

**SePay** là dịch vụ thanh toán tự động qua QR code chuyển khoản ngân hàng. Khi khách hàng chuyển khoản thành công, SePay sẽ gọi webhook đến server của bạn để thông báo.

### Ưu điểm:
- ✅ Không cần tích hợp API phức tạp từng ngân hàng
- ✅ Hỗ trợ tất cả ngân hàng Việt Nam qua VietQR
- ✅ Miễn phí (hoặc phí rất thấp)
- ✅ Webhook realtime

### Luồng hoạt động:
```
1. Backend tạo QR code với thông tin thanh toán
2. User quét QR bằng app ngân hàng
3. User chuyển khoản với nội dung chuẩn (VD: DH123)
4. SePay nhận thông tin giao dịch từ ngân hàng
5. SePay gọi webhook đến backend của bạn
6. Backend xác nhận thanh toán, thông báo cho user
```

---

## Đăng Ký Tài Khoản SePay

### Bước 1: Đăng ký tài khoản
1. Truy cập [https://sepay.vn](https://sepay.vn)
2. Đăng ký tài khoản với email
3. Xác thực email

### Bước 2: Thêm tài khoản ngân hàng
1. Vào **Cài đặt** → **Tài khoản ngân hàng**
2. Thêm tài khoản ngân hàng của bạn
3. Xác thực tài khoản (SePay sẽ gửi 1 giao dịch nhỏ để xác thực)

### Bước 3: Cấu hình Webhook
1. Vào **Cài đặt** → **Webhook**
2. Nhập URL webhook (VD: `https://your-domain.com/api/payments/sepay/webhook`)
3. Lưu lại

### Bước 4: Lấy thông tin cần thiết
Ghi lại các thông tin sau:
- **Bank Code**: Mã ngân hàng (VD: `MB`, `VCB`, `TCB`, ...)
- **Account Number**: Số tài khoản
- **Account Name**: Tên chủ tài khoản

---

## Kiến Trúc Tích Hợp

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│    Frontend     │────▶│    Backend      │────▶│   Database      │
│   (React/Vue)   │     │ (Spring/Node)   │     │  (PostgreSQL)   │
└─────────────────┘     └─────────────────┘     └─────────────────┘
        │                       │
        │                       │
        ▼                       ▼
┌─────────────────┐     ┌─────────────────┐
│   QR Code       │     │    SePay        │
│   Display       │     │   Webhook       │
└─────────────────┘     └─────────────────┘
        │                       │
        ▼                       ▼
┌─────────────────┐     ┌─────────────────┐
│  User scans &   │────▶│   Bank System   │
│  transfers      │     │                 │
└─────────────────┘     └─────────────────┘
```

---

## Backend Implementation (Spring Boot)

### 4.1 Entity: Payment

```java
// domain/model/Payment.java
@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod; // SEPAY, COD

    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // PENDING, SUCCESS, FAILED

    private String transactionId;
    private String description;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = PaymentStatus.PENDING;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### 4.2 Service: SePayService

```java
// application/service/SePayService.java
@Service
@Slf4j
public class SePayService {
    
    // Cấu hình từ application.yml
    @Value("${sepay.bank-code}")
    private String bankCode;
    
    @Value("${sepay.account-number}")
    private String accountNumber;
    
    @Value("${sepay.account-name}")
    private String accountName;

    /**
     * Tạo thông tin thanh toán QR
     * @param orderId ID đơn hàng
     * @param amount Số tiền thanh toán
     * @return Thông tin QR để hiển thị cho user
     */
    public SePayPaymentInfo createPaymentInfo(Long orderId, BigDecimal amount) {
        // Nội dung chuyển khoản (QUAN TRỌNG: Format cố định để parse trong webhook)
        String description = "DH" + orderId;
        
        // Tạo QR URL theo chuẩn VietQR
        String qrCodeUrl = String.format(
            "https://qr.sepay.vn/img?acc=%s&bank=%s&amount=%s&des=%s&template=compact",
            accountNumber,
            bankCode,
            amount.intValue(),
            description
        );
        
        log.info("[SEPAY] Generated QR URL for Order {}: {}", orderId, qrCodeUrl);
        
        return SePayPaymentInfo.builder()
                .orderId(orderId)
                .bankCode(bankCode)
                .accountNumber(accountNumber)
                .accountName(accountName)
                .amount(amount)
                .description(description)
                .qrCodeUrl(qrCodeUrl)
                .build();
    }
    
    @Data
    @Builder
    public static class SePayPaymentInfo {
        private Long orderId;
        private String bankCode;
        private String accountNumber;
        private String accountName;
        private BigDecimal amount;
        private String description;
        private String qrCodeUrl;
    }
}
```

### 4.3 Controller: PaymentController

```java
// interfaces/controller/PaymentController.java
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final SePayService sePayService;
    private final PaymentRepository paymentRepository;
    private final RabbitTemplate rabbitTemplate; // Optional: for event publishing

    /**
     * API tạo QR thanh toán
     * Frontend gọi khi user chọn thanh toán chuyển khoản
     */
    @PostMapping("/sepay/init")
    public ResponseEntity<SePayService.SePayPaymentInfo> initSePayPayment(
            @RequestBody PaymentRequest request) {
        
        // Lưu payment record với status PENDING
        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .paymentMethod(PaymentMethod.SEPAY)
                .status(PaymentStatus.PENDING)
                .build();
        paymentRepository.save(payment);
        
        // Tạo QR info
        SePayService.SePayPaymentInfo paymentInfo = sePayService.createPaymentInfo(
            request.getOrderId(), 
            request.getAmount()
        );
        
        log.info("[PAYMENT] Generated SePay QR for Order {}", request.getOrderId());
        return ResponseEntity.ok(paymentInfo);
    }

    /**
     * Webhook nhận callback từ SePay
     * SePay sẽ gọi endpoint này khi có giao dịch mới
     */
    @PostMapping("/sepay/webhook")
    public ResponseEntity<Map<String, String>> sePayWebhook(
            @RequestBody SePayWebhookRequest webhook) {
        
        log.info("[PAYMENT] Received SePay webhook: transactionId={}, amount={}, content={}", 
                webhook.getTransactionId(), webhook.getTransferAmount(), webhook.getContent());
        
        try {
            // Parse order ID từ nội dung chuyển khoản
            // Format: "DH{orderId}" (VD: "DH123")
            String content = webhook.getContent();
            if (content != null && content.toUpperCase().startsWith("DH")) {
                Long orderId = Long.parseLong(content.substring(2).trim().split("\\s+")[0]);
                
                // Tìm payment record (lấy record mới nhất nếu có duplicate)
                Optional<Payment> paymentOpt = paymentRepository
                    .findFirstByOrderIdOrderByCreatedAtDesc(orderId);
                    
                if (paymentOpt.isPresent()) {
                    Payment payment = paymentOpt.get();
                    payment.setStatus(PaymentStatus.SUCCESS);
                    payment.setTransactionId(webhook.getTransactionId());
                    paymentRepository.save(payment);
                    
                    log.info("[PAYMENT] SePay payment confirmed for Order {}", orderId);
                    
                    // Publish event qua RabbitMQ (optional)
                    publishPaymentConfirmedEvent(orderId, webhook.getTransferAmount(), 
                        webhook.getTransactionId());
                    
                    return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Payment confirmed for order " + orderId
                    ));
                }
            }
            
            return ResponseEntity.ok(Map.of("status", "ignored", "message", "No matching order"));
            
        } catch (Exception e) {
            log.error("[PAYMENT] Error processing SePay webhook: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("status", "error", "message", e.getMessage()));
        }
    }
    
    /**
     * Publish event để service khác (order-service) biết thanh toán thành công
     */
    private void publishPaymentConfirmedEvent(Long orderId, BigDecimal amount, String transactionId) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", "PAYMENT_CONFIRMED");
        event.put("orderId", orderId);
        event.put("paymentStatus", "SUCCESS");
        event.put("transactionId", transactionId);
        event.put("amount", amount);
        event.put("timestamp", System.currentTimeMillis());
        
        rabbitTemplate.convertAndSend("food-ordering-exchange", "payment.confirmed", event);
        log.info("[PAYMENT] Published PAYMENT_CONFIRMED event for Order {}", orderId);
    }
}

// DTO cho webhook request
@Data
class SePayWebhookRequest {
    private String id;
    private String gateway;
    private String transactionDate;
    private String accountNumber;
    private String code;
    private String content;          // Nội dung chuyển khoản (VD: "DH123")
    private BigDecimal transferAmount;
    private BigDecimal accumulated;
    private String subAccount;
    private String referenceCode;
    private String transactionId;    // Mã giao dịch
    private String description;
}
```

### 4.4 Cấu hình application.yml

```yaml
# application.yml cho service-payment
sepay:
  bank-code: MB              # Mã ngân hàng (MB, VCB, TCB, ...)
  account-number: "123456789" # Số tài khoản
  account-name: "NGUYEN VAN A" # Tên chủ tài khoản

spring:
  rabbitmq:
    host: rabbitmq           # Hoặc localhost nếu không dùng Docker
    port: 5672
    username: guest
    password: guest
```

---

## Frontend Implementation (React)

### 5.1 Component: PaymentModal

```jsx
// components/PaymentModal.jsx
import { useState } from 'react'
import { QrCode, X, Copy, Check } from 'lucide-react'
import axios from 'axios'
import toast from 'react-hot-toast'

export default function PaymentModal({ orderId, amount, userId, onClose, onSuccess }) {
  const [qrPaymentInfo, setQrPaymentInfo] = useState(null)
  const [loading, setLoading] = useState(false)
  const [copied, setCopied] = useState(false)

  // Lấy QR code khi component mount
  useEffect(() => {
    initPayment()
  }, [])

  const initPayment = async () => {
    setLoading(true)
    try {
      const response = await axios.post('/api/payments/sepay/init', {
        orderId,
        userId,
        amount
      })
      setQrPaymentInfo(response.data)
    } catch (err) {
      toast.error('Không thể tạo mã thanh toán')
      onClose()
    } finally {
      setLoading(false)
    }
  }

  const handleCopyAccount = () => {
    navigator.clipboard.writeText(qrPaymentInfo?.accountNumber || '')
    setCopied(true)
    toast.success('Đã copy số tài khoản!')
    setTimeout(() => setCopied(false), 2000)
  }

  if (loading || !qrPaymentInfo) {
    return (
      <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50">
        <div className="bg-white rounded-2xl p-8 text-center">
          <div className="animate-spin w-8 h-8 border-4 border-blue-500 border-t-transparent rounded-full mx-auto"></div>
          <p className="mt-4">Đang tạo mã thanh toán...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-3xl max-w-md w-full p-6 relative">
        <button onClick={onClose} className="absolute top-4 right-4 p-2 hover:bg-gray-100 rounded-full">
          <X size={24} />
        </button>
        
        {/* Header */}
        <div className="text-center mb-4">
          <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-3">
            <QrCode size={24} className="text-blue-500" />
          </div>
          <h3 className="text-xl font-bold">Quét mã để thanh toán</h3>
          <p className="text-gray-500 text-sm">Sử dụng app ngân hàng để quét mã QR</p>
        </div>
        
        {/* QR Code */}
        <div className="bg-gray-50 rounded-2xl p-4 mb-4">
          <img 
            src={qrPaymentInfo.qrCodeUrl} 
            alt="QR Payment" 
            className="w-full max-w-[200px] mx-auto rounded-xl shadow-lg"
          />
        </div>
        
        {/* Payment Info */}
        <div className="space-y-2 text-sm mb-4">
          <div className="flex justify-between py-2 border-b">
            <span className="text-gray-500">Ngân hàng</span>
            <span className="font-bold">{qrPaymentInfo.bankCode}</span>
          </div>
          <div className="flex justify-between py-2 border-b">
            <span className="text-gray-500">Số tài khoản</span>
            <div className="flex items-center gap-2">
              <span className="font-bold">{qrPaymentInfo.accountNumber}</span>
              <button onClick={handleCopyAccount} className="p-1 hover:bg-gray-100 rounded">
                {copied ? <Check size={14} className="text-green-500" /> : <Copy size={14} />}
              </button>
            </div>
          </div>
          <div className="flex justify-between py-2 border-b">
            <span className="text-gray-500">Chủ tài khoản</span>
            <span className="font-bold">{qrPaymentInfo.accountName}</span>
          </div>
          <div className="flex justify-between py-2 border-b">
            <span className="text-gray-500">Số tiền</span>
            <span className="font-bold text-orange-500">{qrPaymentInfo.amount?.toLocaleString()}đ</span>
          </div>
          <div className="flex justify-between py-2">
            <span className="text-gray-500">Nội dung CK</span>
            <span className="font-bold text-blue-600">{qrPaymentInfo.description}</span>
          </div>
        </div>
        
        {/* Warning */}
        <div className="bg-yellow-50 border border-yellow-200 rounded-xl p-3 mb-4">
          <p className="text-xs text-yellow-800">
            ⚠️ <strong>Quan trọng:</strong> Nhập đúng nội dung CK <strong className="text-blue-600">{qrPaymentInfo.description}</strong>
          </p>
        </div>
        
        <button
          onClick={() => {
            onClose()
            toast.success('Đã ghi nhận! Đơn hàng sẽ cập nhật khi thanh toán thành công.')
          }}
          className="w-full py-3 bg-green-500 text-white rounded-xl font-bold hover:bg-green-600"
        >
          ✓ Tôi đã thanh toán
        </button>
      </div>
    </div>
  )
}
```

### 5.2 Sử dụng Component

```jsx
// pages/CheckoutPage.jsx
import PaymentModal from '../components/PaymentModal'

function CheckoutPage() {
  const [showPayment, setShowPayment] = useState(false)
  const [orderId, setOrderId] = useState(null)
  
  const handleCheckout = async () => {
    // 1. Tạo order
    const response = await axios.post('/api/orders', orderData)
    const newOrderId = response.data.id
    setOrderId(newOrderId)
    
    // 2. Nếu chọn chuyển khoản, show payment modal
    if (paymentMethod === 'SEPAY') {
      setShowPayment(true)
    }
  }
  
  return (
    <div>
      {/* Checkout form... */}
      
      {showPayment && (
        <PaymentModal
          orderId={orderId}
          amount={totalAmount}
          userId={user.id}
          onClose={() => setShowPayment(false)}
          onSuccess={() => {
            setShowPayment(false)
            // Navigate to success page
          }}
        />
      )}
    </div>
  )
}
```

---

## Xử Lý Webhook

### 6.1 Flow xử lý webhook

```
SePay Webhook → Backend
      │
      ▼
Parse nội dung chuyển khoản (content = "DH{orderId}")
      │
      ▼
Tìm Payment record theo orderId
      │
      ▼
Update status = SUCCESS
      │
      ▼
Publish event PAYMENT_CONFIRMED (RabbitMQ)
      │
      ▼
Order Service nhận event → Update order.paymentStatus
      │
      ▼
Publish PAYMENT_SUCCESS → Socket Service → WebSocket → Frontend
```

### 6.2 Order Service nhận event

```java
// service-order/PaymentEventListener.java
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final OrderRepository orderRepository;
    private final AmqpTemplate rabbitTemplate;

    @RabbitListener(queues = "payment-confirmed-queue")
    public void handlePaymentConfirmed(Map<String, Object> event) {
        try {
            String type = (String) event.get("type");
            if (!"PAYMENT_CONFIRMED".equals(type)) return;

            Long orderId = Long.valueOf(event.get("orderId").toString());
            String paymentStatus = (String) event.get("paymentStatus");

            // Cập nhật paymentStatus trong Order
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                order.setPaymentStatus(paymentStatus);
                orderRepository.save(order);
                
                // Gửi notification đến user
                sendPaymentNotification(order);
            }
        } catch (Exception e) {
            log.error("Error processing payment event: {}", e.getMessage());
        }
    }

    private void sendPaymentNotification(Order order) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "PAYMENT_SUCCESS");
        notification.put("orderId", order.getId());
        notification.put("userId", order.getUserId());
        notification.put("message", "Đơn hàng #" + order.getId() + " đã thanh toán thành công!");

        rabbitTemplate.convertAndSend("food-ordering-exchange", "order.status.changed", notification);
    }
}
```

---

## Realtime Notification

### 7.1 Socket Service

```java
// service-socket/SocketEventListener.java
@Component
@RequiredArgsConstructor
@Slf4j
public class SocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = "socket-notification-queue")
    public void handleEvent(Map<String, Object> event) {
        String type = (String) event.get("type");
        
        if ("PAYMENT_SUCCESS".equals(type)) {
            String userId = String.valueOf(event.get("userId"));
            
            Map<String, Object> notification = new HashMap<>(event);
            notification.put("subject", "Thanh toán thành công 🎉");
            notification.put("createdAt", new Date());
            
            // Push đến topic của user
            messagingTemplate.convertAndSend("/topic/user/" + userId, notification);
            log.info("Pushed PAYMENT_SUCCESS to user {}", userId);
        }
    }
}
```

### 7.2 Frontend WebSocket

```javascript
// services/socketService.js
import SockJS from 'sockjs-client'
import { Stomp } from '@stomp/stompjs'

let stompClient = null

export const connectSocket = (onNotification, userId) => {
  const socket = new SockJS('/ws')
  stompClient = Stomp.over(socket)
  
  stompClient.connect({}, () => {
    console.log('Connected to WebSocket')
    
    // Subscribe to user's topic
    stompClient.subscribe(`/topic/user/${userId}`, (message) => {
      const notification = JSON.parse(message.body)
      onNotification(notification)
    })
  })
  
  return stompClient
}

export const disconnectSocket = () => {
  if (stompClient) {
    stompClient.disconnect()
  }
}
```

### 7.3 Handle notification trong component

```jsx
// Trong component cần nhận notification (VD: Navbar hoặc CheckoutPage)
useEffect(() => {
  if (user) {
    connectSocket((notification) => {
      if (notification.type === 'PAYMENT_SUCCESS') {
        // Hiển thị success modal hoặc toast
        setPaymentSuccess(true)
        toast.success(notification.message)
      }
    }, user.id)
  }
  
  return () => disconnectSocket()
}, [user])
```

---

## Testing với ngrok

Vì SePay cần gọi webhook đến server của bạn, khi develop local bạn cần dùng ngrok để expose localhost.

### 8.1 Cài đặt ngrok

```bash
# Download từ https://ngrok.com/download
# Hoặc dùng npm
npm install -g ngrok
```

### 8.2 Chạy ngrok

```bash
# Expose port 8080 (API Gateway)
ngrok http 8080
```

Bạn sẽ nhận được URL như: `https://abc123.ngrok.io`

### 8.3 Cấu hình webhook trên SePay

Vào SePay Dashboard → Webhook → Nhập URL:
```
https://abc123.ngrok.io/api/payments/sepay/webhook
```

### 8.4 Test thanh toán

1. Tạo đơn hàng và chọn chuyển khoản
2. Quét mã QR hoặc chuyển khoản thủ công
3. Nhập đúng nội dung CK (VD: "DH123")
4. Chờ khoảng 5-10 giây
5. Kiểm tra log backend và frontend

---

## Checklist Triển Khai

### ✅ Backend
- [ ] Tạo entity Payment
- [ ] Tạo PaymentRepository
- [ ] Tạo SePayService với `createPaymentInfo()`
- [ ] Tạo PaymentController với `/sepay/init` và `/sepay/webhook`
- [ ] Cấu hình RabbitMQ (exchange, queue, binding)
- [ ] Tạo PaymentEventListener trong Order Service
- [ ] Cấu hình SePay credentials trong application.yml

### ✅ Frontend
- [ ] Tạo PaymentModal component
- [ ] Tích hợp vào checkout flow
- [ ] Kết nối WebSocket để nhận notification
- [ ] Hiển thị success modal khi nhận PAYMENT_SUCCESS

### ✅ SePay Dashboard
- [ ] Đăng ký tài khoản SePay
- [ ] Thêm và xác thực tài khoản ngân hàng
- [ ] Cấu hình webhook URL

### ✅ Deployment
- [ ] Cấu hình HTTPS cho webhook (bắt buộc cho production)
- [ ] Đổi ngrok URL sang domain thật
- [ ] Test end-to-end

---

## Troubleshooting

### Webhook không được gọi
- Kiểm tra URL webhook trên SePay Dashboard
- Đảm bảo endpoint public (không require auth)
- Kiểm tra ngrok đang chạy

### Không parse được orderId
- Kiểm tra format nội dung CK: phải là "DH" + số
- User có thể gõ thêm text → dùng regex hoặc split để lấy số

### Payment không update trong database
- Kiểm tra có duplicate Payment record không
- Dùng `findFirstByOrderIdOrderByCreatedAtDesc()` thay vì `findByOrderId()`

### WebSocket không nhận notification
- Kiểm tra RabbitMQ đang chạy
- Kiểm tra binding giữa exchange và queue
- Kiểm tra user đang subscribe đúng topic

---

> 📌 **Lưu ý**: Tài liệu này dựa trên hệ thống Food Ordering. Bạn cần điều chỉnh entity names, routing keys, và logic business phù hợp với dự án của mình.
