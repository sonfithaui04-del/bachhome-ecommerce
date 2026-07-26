package com.bachhome.ai.application;

import com.bachhome.ai.application.dto.ChatRequest;
import com.bachhome.ai.application.dto.ChatResponse;
import com.bachhome.ai.infrastructure.GeminiClient;
import com.bachhome.ai.infrastructure.ProductContextProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Điều phối nghiệp vụ chatbot:
 * 1) Lấy danh sách sản phẩm từ service-product.
 * 2) Dựng system prompt (vai trò trợ lý bán đồ gia dụng).
 * 3) Gọi Gemini lấy câu trả lời.
 * 4) Dò các sản phẩm được nhắc trong câu trả lời để trả kèm ảnh.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private static final int MAX_SUGGESTIONS = 4;

    private final GeminiClient geminiClient;
    private final ProductContextProvider productContextProvider;

    @Value("${ai.shop-name:BachHome}")
    private String shopName;

    @Value("${ai.shop-desc:cửa hàng đồ gia dụng}")
    private String shopDesc;

    @Value("${ai.use-product-context:true}")
    private boolean useProductContext;

    public ChatResponse chat(ChatRequest req) {
        if (!geminiClient.isConfigured()) {
            log.warn("[AI] Chưa cấu hình GEMINI_API_KEY.");
            return new ChatResponse(
                    "Chatbot chưa được cấu hình khóa API (GEMINI_API_KEY). Vui lòng liên hệ quản trị viên.",
                    List.of());
        }

        List<Map<String, Object>> products = useProductContext
                ? productContextProvider.fetchProducts()
                : List.of();
        String productContext = productContextProvider.buildContext(products);
        String systemPrompt = buildSystemPrompt(productContext);

        List<Map<String, Object>> contents = new ArrayList<>();
        if (req.getHistory() != null) {
            for (ChatRequest.Message m : req.getHistory()) {
                if (m == null || m.getContent() == null || m.getContent().isBlank()) {
                    continue;
                }
                String role = "model".equalsIgnoreCase(m.getRole()) ? "model" : "user";
                contents.add(Map.of("role", role, "parts", List.of(Map.of("text", m.getContent()))));
            }
        }
        contents.add(Map.of("role", "user", "parts", List.of(Map.of("text", req.getMessage()))));

        String reply = geminiClient.generate(systemPrompt, contents);
        List<ChatResponse.Suggestion> suggestions = matchSuggestions(reply, products);
        return new ChatResponse(reply, suggestions);
    }

    private String buildSystemPrompt(String productContext) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là trợ lý tư vấn bán hàng của ").append(shopName)
                .append(" - ").append(shopDesc).append(". ");
        sb.append("Nhiệm vụ của bạn: tư vấn và gợi ý sản phẩm phù hợp với nhu cầu của khách hàng. ");
        sb.append("Hãy trả lời ngắn gọn, thân thiện, lịch sự và luôn bằng tiếng Việt. ");
        sb.append("Chỉ tư vấn về các mặt hàng gia dụng mà cửa hàng đang bán. ");
        sb.append("Nếu khách hỏi ngoài phạm vi bán hàng, hãy lịch sự từ chối và hướng khách quay lại sản phẩm. ");
        sb.append("Khi gợi ý sản phẩm, hãy ghi CHÍNH XÁC nguyên văn tên sản phẩm như trong danh sách bên dưới ");
        sb.append("(không thêm ngoặc hay đổi tên), để hệ thống hiển thị đúng sản phẩm kèm ảnh cho khách. ");
        if (productContext != null && !productContext.isBlank()) {
            sb.append("\n\nDanh sách sản phẩm hiện có của cửa hàng ");
            sb.append("(chỉ được gợi ý các sản phẩm trong danh sách này, không bịa thêm):\n");
            sb.append(productContext);
        } else {
            sb.append("Hiện chưa lấy được danh sách sản phẩm cụ thể, hãy tư vấn chung về đồ gia dụng ");
            sb.append("và mời khách xem thêm trên website.");
        }
        return sb.toString();
    }

    /**
     * Dò tên sản phẩm xuất hiện trong câu trả lời để trả kèm ảnh.
     * So khớp sau khi chuẩn hoá (bỏ dấu câu, gộp khoảng trắng, thường hoá).
     */
    private List<ChatResponse.Suggestion> matchSuggestions(String reply, List<Map<String, Object>> products) {
        List<ChatResponse.Suggestion> result = new ArrayList<>();
        if (reply == null || reply.isBlank() || products == null || products.isEmpty()) {
            return result;
        }
        String normalizedReply = normalize(reply);
        for (Map<String, Object> p : products) {
            Object nameObj = p.get("name");
            if (nameObj == null) {
                continue;
            }
            String normalizedName = normalize(nameObj.toString());
            if (normalizedName.isBlank()) {
                continue;
            }
            if (normalizedReply.contains(normalizedName)) {
                result.add(toSuggestion(p));
                if (result.size() >= MAX_SUGGESTIONS) {
                    break;
                }
            }
        }
        return result;
    }

    private ChatResponse.Suggestion toSuggestion(Map<String, Object> p) {
        Long id = p.get("id") != null ? Long.valueOf(p.get("id").toString()) : null;
        String name = p.get("name") != null ? p.get("name").toString() : "";
        BigDecimal price = p.get("price") != null ? new BigDecimal(p.get("price").toString()) : null;
        String imageUrl = p.get("imageUrl") != null ? p.get("imageUrl").toString() : null;
        return new ChatResponse.Suggestion(id, name, price, imageUrl);
    }

    /** Chuẩn hoá: thường hoá + thay ký tự không phải chữ/số bằng khoảng trắng + gộp khoảng trắng. */
    private String normalize(String s) {
        return s.toLowerCase()
                .replaceAll("[^\\p{L}\\p{Nd}]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
