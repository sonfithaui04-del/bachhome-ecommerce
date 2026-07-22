package com.foodordering.notification.interfaces.controller;

import com.foodordering.notification.domain.ChatMessage;
import com.foodordering.notification.domain.ChatMessageRepository;
import com.foodordering.notification.config.RabbitMQConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
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
}
