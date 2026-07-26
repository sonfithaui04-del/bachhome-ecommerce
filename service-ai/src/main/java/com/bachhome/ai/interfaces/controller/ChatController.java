package com.bachhome.ai.interfaces.controller;

import com.bachhome.ai.application.ChatService;
import com.bachhome.ai.application.dto.ChatRequest;
import com.bachhome.ai.application.dto.ChatResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API cho Chatbot AI.
 * Qua API Gateway: POST /api/ai/chat  (StripPrefix=1 -> /ai/chat)
 */
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        log.info("[AI-CONTROLLER] Nhận câu hỏi: {}", request.getMessage());
        return ResponseEntity.ok(chatService.chat(request));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("AI Service is running");
    }
}
