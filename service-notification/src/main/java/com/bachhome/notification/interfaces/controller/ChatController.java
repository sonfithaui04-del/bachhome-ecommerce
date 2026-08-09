package com.bachhome.notification.interfaces.controller;

import com.bachhome.notification.domain.ChatMessage;
import com.bachhome.notification.domain.ChatMessageRepository;
import com.bachhome.notification.domain.model.Notification;
import com.bachhome.notification.infrastructure.repository.NotificationRepository;
import com.bachhome.notification.config.RabbitMQConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chat", description = "API cho hệ thống Live Chat")
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;
    private final NotificationRepository notificationRepository;
    private final RabbitTemplate rabbitTemplate;

    @PostMapping("/send")
    @Operation(summary = "Gửi tin nhắn chat")
    public ResponseEntity<ChatMessage> sendMessage(@RequestBody ChatMessage message) {
        log.info("[CHAT-CONTROLLER] Received message from {} for order {}", message.getSenderName(), message.getOrderId());
        
        message.setTimestamp(LocalDateTime.now());
        ChatMessage saved = chatMessageRepository.save(message);

        // Prepare event for RabbitMQ
        Map<String, Object> event = new HashMap<>();
        event.put("type", "CHAT_MESSAGE");
        event.put("id", saved.getId());
        event.put("orderId", saved.getOrderId());
        event.put("senderId", saved.getSenderId());
        event.put("senderName", saved.getSenderName());
        event.put("message", saved.getMessage());
        event.put("timestamp", saved.getTimestamp().toString());

        // Publish to RabbitMQ so Socket Service can relay it
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.CHAT_ROUTING_KEY, event);
        log.info("[CHAT-CONTROLLER] Published chat message event for order {}", saved.getOrderId());

        // Tạo thêm một thông báo cho người nhận, để tin hiện ở chuông thông báo
        // dù họ đang ở trang nào — và vì lưu DB nên tải lại trang vẫn còn.
        publishChatNotification(message.getRecipientId(), saved);

        return ResponseEntity.ok(saved);
    }

    /**
     * Sinh thông báo cho phía đối diện của cuộc hội thoại.
     * - Có recipientId (admin trả lời khách): lưu Notification + đẩy vào kênh riêng của khách.
     * - Không có (khách nhắn cho shop): chỉ đẩy vào kênh chung của admin, không lưu
     *   vì thông báo admin không gắn với một userId cụ thể nào.
     */
    private void publishChatNotification(Long recipientId, ChatMessage saved) {
        String subject = "Tin nhắn mới từ " + saved.getSenderName();
        String preview = saved.getMessage().length() > 120
                ? saved.getMessage().substring(0, 120) + "…"
                : saved.getMessage();

        Map<String, Object> notify = new HashMap<>();
        notify.put("type", "CHAT_NOTIFICATION");
        notify.put("orderId", saved.getOrderId());
        notify.put("subject", subject);
        notify.put("message", preview);
        notify.put("senderId", saved.getSenderId());
        notify.put("senderName", saved.getSenderName());
        notify.put("createdAt", saved.getTimestamp().toString());

        if (recipientId != null) {
            Notification notification = notificationRepository.save(Notification.builder()
                    .userId(recipientId)
                    .type("CHAT")
                    .orderId(saved.getOrderId())
                    .subject(subject)
                    .message(preview)
                    .sent(true)
                    .build());

            notify.put("id", notification.getId());
            notify.put("userId", recipientId);
            log.info("[CHAT-CONTROLLER] Saved CHAT notification {} for user {}", notification.getId(), recipientId);
        } else {
            notify.put("toAdmin", true);
            log.info("[CHAT-CONTROLLER] Chat notification for admins, order {}", saved.getOrderId());
        }

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.CHAT_ROUTING_KEY, notify);
    }

    @GetMapping("/history/{orderId}")
    @Operation(summary = "Lấy lịch sử chat của đơn hàng")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable Long orderId) {
        log.info("[CHAT-CONTROLLER] Fetching chat history for order {}", orderId);
        List<ChatMessage> history = chatMessageRepository.findByOrderIdOrderByTimestampAsc(orderId);
        return ResponseEntity.ok(history);
    }

    /**
     * Danh sách hội thoại cho trang hỗ trợ của admin: mỗi đơn hàng một dòng,
     * kèm tin nhắn cuối cùng. Sắp xếp đơn có tin mới nhất lên đầu.
     */
    @GetMapping("/conversations")
    @Operation(summary = "Danh sách hội thoại theo đơn hàng (Admin)")
    public ResponseEntity<List<Map<String, Object>>> getConversations() {
        List<ChatMessage> all = chatMessageRepository.findAllByOrderByTimestampDesc();

        // LinkedHashMap giữ nguyên thứ tự duyệt (mới nhất trước), và vì danh sách
        // đã sắp giảm dần nên tin đầu tiên gặp của mỗi đơn chính là tin mới nhất.
        Map<Long, Map<String, Object>> byOrder = new LinkedHashMap<>();

        for (ChatMessage m : all) {
            Map<String, Object> conv = byOrder.get(m.getOrderId());
            if (conv == null) {
                conv = new HashMap<>();
                conv.put("orderId", m.getOrderId());
                conv.put("lastMessage", m.getMessage());
                conv.put("lastSenderId", m.getSenderId());
                conv.put("lastSenderName", m.getSenderName());
                conv.put("lastTimestamp", m.getTimestamp().toString());
                conv.put("total", 0);
                byOrder.put(m.getOrderId(), conv);
            }
            conv.put("total", ((Integer) conv.get("total")) + 1);
        }

        log.info("[CHAT-CONTROLLER] Found {} conversations", byOrder.size());
        return ResponseEntity.ok(new ArrayList<>(byOrder.values()));
    }
}
