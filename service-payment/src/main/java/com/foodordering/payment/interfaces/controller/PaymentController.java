package com.foodordering.payment.interfaces.controller;

import com.foodordering.payment.application.service.SePayService;
import com.foodordering.payment.domain.model.Payment;
import com.foodordering.payment.domain.model.PaymentMethod;
import com.foodordering.payment.domain.model.PaymentStatus;
import com.foodordering.payment.infrastructure.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Payment Controller - Xử lý thanh toán COD và SePay
 */
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    
    private final PaymentRepository paymentRepository;
    private final SePayService sePayService;
    private final RabbitTemplate rabbitTemplate;
    
    /**
     * Thanh toán COD - Tự động approve
     */
    @PostMapping
    public ResponseEntity<Payment> processPayment(@RequestBody PaymentRequest request) {
        log.info("[PAYMENT] Processing COD payment for Order {}", request.getOrderId());
        
        Payment payment = Payment.builder()
            .orderId(request.getOrderId())
            .userId(request.getUserId())
            .amount(request.getAmount())
            .paymentMethod(PaymentMethod.COD)
            .build();
        payment.approve(); // Auto-approve for COD
        
        return ResponseEntity.ok(paymentRepository.save(payment));
    }
    
    /**
     * Tạo thanh toán SePay - Trả về QR code URL
     */
    @PostMapping("/sepay/init")
    public ResponseEntity<SePayService.SePayPaymentInfo> initSePayPayment(@RequestBody PaymentRequest request) {
        log.info("[PAYMENT] Initiating SePay payment for Order {}, amount: {}", 
                request.getOrderId(), request.getAmount());
        
        // Tạo payment record với trạng thái PENDING
        Payment payment = Payment.builder()
            .orderId(request.getOrderId())
            .userId(request.getUserId())
            .amount(request.getAmount())
            .paymentMethod(PaymentMethod.SEPAY)
            .status(PaymentStatus.PENDING)
            .build();
        
        Payment savedPayment = paymentRepository.save(payment);
        
        // Tạo thông tin QR code
        SePayService.SePayPaymentInfo paymentInfo = sePayService.createPaymentInfo(
            request.getOrderId(), 
            request.getAmount()
        );
        
        log.info("[PAYMENT] Generated SePay QR for Order {}", request.getOrderId());
        return ResponseEntity.ok(paymentInfo);
    }
    
    /**
     * Webhook callback từ SePay khi có giao dịch
     * SePay sẽ gọi endpoint này khi khách hàng chuyển khoản thành công
     */
    @PostMapping("/sepay/webhook")
    public ResponseEntity<Map<String, String>> sePayWebhook(@RequestBody SePayWebhookRequest webhook) {
        log.info("[PAYMENT] Received SePay webhook: transactionId={}, amount={}, content={}", 
                webhook.getTransactionId(), webhook.getTransferAmount(), webhook.getContent());
        
        try {
            // Parse order ID từ nội dung chuyển khoản (format: DH{orderId})
            String content = webhook.getContent();
            if (content != null && content.toUpperCase().startsWith("DH")) {
                Long orderId = Long.parseLong(content.substring(2).trim().split("\\s+")[0]);
                
                // Tìm và cập nhật payment
                Optional<Payment> paymentOpt = paymentRepository.findFirstByOrderIdOrderByCreatedAtDesc(orderId);
                if (paymentOpt.isPresent()) {
                    Payment payment = paymentOpt.get();
                    payment.setStatus(PaymentStatus.SUCCESS);
                    payment.setTransactionId(webhook.getTransactionId());
                    paymentRepository.save(payment);
                    
                    log.info("[PAYMENT] SePay payment confirmed for Order {}", orderId);
                    
                    // Gửi event qua RabbitMQ để cập nhật Order status
                    publishPaymentConfirmedEvent(orderId, webhook.getTransferAmount(), webhook.getTransactionId());
                    
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
     * Kiểm tra trạng thái thanh toán theo Order ID
     */
    @GetMapping("/status/{orderId}")
    public ResponseEntity<PaymentStatusResponse> getPaymentStatus(@PathVariable Long orderId) {
        Optional<Payment> payment = paymentRepository.findByOrderId(orderId);
        
        if (payment.isPresent()) {
            Payment p = payment.get();
            return ResponseEntity.ok(new PaymentStatusResponse(
                p.getOrderId(),
                p.getStatus().name(),
                p.getPaymentMethod().name(),
                p.getTransactionId()
            ));
        }
        
        return ResponseEntity.ok(new PaymentStatusResponse(orderId, "NOT_FOUND", null, null));
    }
    
    /**
     * User xác nhận đã thanh toán (cho trường hợp webhook chưa nhận được)
     */
    @PostMapping("/sepay/confirm/{orderId}")
    public ResponseEntity<Map<String, String>> confirmPayment(@PathVariable Long orderId) {
        log.info("[PAYMENT] Manual confirmation for Order {}", orderId);
        
        Optional<Payment> paymentOpt = paymentRepository.findByOrderId(orderId);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            // Chuyển sang PENDING_VERIFICATION để admin xác nhận
            payment.setStatus(PaymentStatus.PENDING_VERIFICATION);
            paymentRepository.save(payment);
            
            return ResponseEntity.ok(Map.of(
                "status", "pending_verification",
                "message", "Đã nhận yêu cầu xác nhận. Vui lòng chờ xác nhận từ hệ thống."
            ));
        }
        
        return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Payment not found"));
    }
    
    /**
     * Publish payment confirmed event to RabbitMQ
     * Order-service sẽ listen và cập nhật paymentStatus
     */
    private void publishPaymentConfirmedEvent(Long orderId, BigDecimal amount, String transactionId) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("type", "PAYMENT_CONFIRMED");
            event.put("orderId", orderId);
            event.put("amount", amount);
            event.put("transactionId", transactionId);
            event.put("paymentStatus", "SUCCESS");
            event.put("timestamp", System.currentTimeMillis());
            
            rabbitTemplate.convertAndSend("food-ordering-exchange", "payment.confirmed", event);
            log.info("[PAYMENT] Published PAYMENT_CONFIRMED event for Order {}", orderId);
        } catch (Exception e) {
            log.error("[PAYMENT] Failed to publish event: {}", e.getMessage());
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Payment Service is running");
    }
}

// DTOs
@lombok.Data
class PaymentRequest {
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String paymentMethod;
}

@lombok.Data
class SePayWebhookRequest {
    private String id;
    private String gateway;
    private String transactionDate;
    private String accountNumber;
    private String code;
    private String content;
    private String transferType;
    private BigDecimal transferAmount;
    private BigDecimal accumulated;
    private String subAccount;
    private String referenceCode;
    private String transactionId;
    private String description;
}

@lombok.Data
@lombok.AllArgsConstructor
class PaymentStatusResponse {
    private Long orderId;
    private String status;
    private String paymentMethod;
    private String transactionId;
}
