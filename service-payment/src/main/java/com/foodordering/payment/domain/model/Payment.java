package com.foodordering.payment.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment Entity - Lưu thông tin giao dịch thanh toán
 */
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
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;
    
    @Column(length = 100)
    private String transactionId;
    
    @Column(length = 500)
    private String qrCodeUrl;
    
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = PaymentStatus.PENDING;
        if (paymentMethod == null) paymentMethod = PaymentMethod.COD;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Approve thanh toán (dùng cho COD)
     */
    public void approve() {
        this.status = PaymentStatus.SUCCESS;
        this.transactionId = "TXN-" + System.currentTimeMillis();
    }
    
    /**
     * Xác nhận thanh toán thành công (dùng cho SePay webhook)
     */
    public void confirmPayment(String transactionId) {
        this.status = PaymentStatus.SUCCESS;
        this.transactionId = transactionId;
    }
    
    /**
     * Đánh dấu thanh toán thất bại
     */
    public void fail() {
        this.status = PaymentStatus.FAILED;
    }
}
