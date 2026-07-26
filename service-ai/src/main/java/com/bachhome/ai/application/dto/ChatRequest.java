package com.bachhome.ai.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * Yêu cầu chat từ khách hàng.
 * - message: câu hỏi hiện tại.
 * - history: lịch sử hội thoại (tùy chọn) để chatbot nhớ ngữ cảnh.
 */
@Data
public class ChatRequest {

    @NotBlank(message = "Nội dung tin nhắn không được để trống")
    private String message;

    private List<Message> history;

    @Data
    public static class Message {
        /** "user" (khách) hoặc "model" (chatbot) */
        private String role;
        private String content;
    }
}
