package com.bachhome.notification.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long senderId;

    private String senderName;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    /**
     * Người nhận tin nhắn này, do phía gửi cung cấp (admin biết đơn hàng thuộc về
     * khách nào). Chỉ dùng để tạo thông báo, không lưu vào bảng chat_messages.
     */
    @Transient
    private Long recipientId;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
