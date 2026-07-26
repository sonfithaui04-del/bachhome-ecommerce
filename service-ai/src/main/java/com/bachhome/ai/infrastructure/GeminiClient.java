package com.bachhome.ai.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gọi Google Gemini (Generative Language API) để sinh câu trả lời.
 * Tài liệu: https://ai.google.dev/api/generate-content
 */
@Component
@Slf4j
public class GeminiClient {

    @Value("${gemini.api-key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-1.5-flash}")
    private String model;

    @Value("${gemini.base-url:https://generativelanguage.googleapis.com/v1beta}")
    private String baseUrl;

    private final RestClient restClient = RestClient.create();

    /** Đã cấu hình API key hay chưa. */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * @param systemInstruction chỉ dẫn hệ thống (vai trò trợ lý + danh sách sản phẩm)
     * @param contents          lịch sử hội thoại theo định dạng Gemini
     * @return câu trả lời dạng text
     */
    public String generate(String systemInstruction, List<Map<String, Object>> contents) {
        String url = baseUrl + "/models/" + model + ":generateContent?key=" + apiKey;

        Map<String, Object> body = new HashMap<>();
        body.put("systemInstruction", Map.of("parts", List.of(Map.of("text", systemInstruction))));
        body.put("contents", contents);

        try {
            JsonNode resp = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

            if (resp == null) {
                return fallback();
            }
            JsonNode text = resp.at("/candidates/0/content/parts/0/text");
            if (text.isMissingNode() || text.asText().isBlank()) {
                log.warn("[AI] Phản hồi Gemini không có nội dung: {}", resp);
                return fallback();
            }
            return text.asText().trim();
        } catch (Exception e) {
            log.error("[AI] Lỗi khi gọi Gemini: {}", e.getMessage());
            return "Xin lỗi, trợ lý đang bận. Bạn vui lòng thử lại sau ít phút nhé.";
        }
    }

    private String fallback() {
        return "Xin lỗi, hiện mình chưa trả lời được câu này. Bạn thử hỏi cách khác giúp mình nhé.";
    }
}
