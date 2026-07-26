package com.bachhome.ai.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Phản hồi của chatbot: câu trả lời + danh sách sản phẩm gợi ý (kèm ảnh).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponse {
    private String reply;
    private List<Suggestion> suggestions;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Suggestion {
        private Long id;
        private String name;
        private BigDecimal price;
        private String imageUrl;
    }
}
