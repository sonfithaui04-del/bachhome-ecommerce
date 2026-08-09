package com.bachhome.notification.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private String type; // EMAIL, SMS, CHAT

    /** Đơn hàng liên quan (dùng cho thông báo CHAT để bấm vào là mở đúng khung chat). */
    @Column(name = "order_id")
    private Long orderId;
    
    @Column(nullable = false, length = 200)
    private String subject;
    
    @Column(nullable = false, length = 1000)
    private String message;
    
    @Column(nullable = false)
    private Boolean sent;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (sent == null) sent = false;
    }
    
    public void markAsSent() {
        this.sent = true;
    }
}
