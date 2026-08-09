package com.bachhome.notification.interfaces.controller;

import com.bachhome.notification.domain.ChatMessage;
import com.bachhome.notification.domain.ChatMessageRepository;
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

        return ResponseEntity.ok(saved);
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
