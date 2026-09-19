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
 * 1) Lấy kho hàng từ service-product rồi chọn ra những món sát với câu khách vừa hỏi.
 * 2) Dựng system prompt (vai trò trợ lý bán đồ gia dụng) gồm bản tóm tắt toàn cửa hàng
 *    và danh sách chi tiết các món đã chọn.
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

        // Cả kho hàng (lấy từ bộ nhớ đệm) và rổ hàng sát với câu khách vừa hỏi.
        List<Map<String, Object>> allProducts = useProductContext
                ? productContextProvider.fetchProducts()
                : List.of();
        List<Map<String, Object>> products =
                productContextProvider.selectRelevant(req.getMessage(), allProducts);
        String catalogSummary = productContextProvider.buildCatalogSummary(allProducts);
        String productContext = productContextProvider.buildContext(products);
        String systemPrompt = buildSystemPrompt(catalogSummary, productContext);

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
        // Dò tên trên toàn kho chứ không chỉ rổ hàng vừa gửi đi, để món nào được nhắc cũng ra ảnh.
        List<ChatResponse.Suggestion> suggestions = matchSuggestions(reply, allProducts);
        return new ChatResponse(reply, suggestions);
    }

    private String buildSystemPrompt(String catalogSummary, String productContext) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là trợ lý tư vấn bán hàng của ").append(shopName)
                .append(" - ").append(shopDesc).append(". ");
        sb.append("Nhiệm vụ của bạn: tư vấn và gợi ý sản phẩm phù hợp với nhu cầu của khách hàng. ");
        sb.append("Hãy trả lời ngắn gọn, thân thiện, lịch sự và luôn bằng tiếng Việt. ");
        sb.append("Chỉ tư vấn về các mặt hàng gia dụng mà cửa hàng đang bán. ");
        sb.append("Nếu khách hỏi ngoài phạm vi bán hàng, hãy lịch sự từ chối và hướng khách quay lại sản phẩm. ");
        sb.append("Khi gợi ý sản phẩm, hãy ghi CHÍNH XÁC nguyên văn tên sản phẩm như trong danh sách bên dưới ");
        sb.append("(không thêm ngoặc hay đổi tên), để hệ thống hiển thị đúng sản phẩm kèm ảnh cho khách. ");
        if (catalogSummary != null && !catalogSummary.isBlank()) {
            sb.append("\n\nQuy mô cửa hàng (dùng để trả lời câu hỏi chung về mặt hàng và tầm giá):\n");
            sb.append(catalogSummary);
        }
        if (productContext != null && !productContext.isBlank()) {
            sb.append("\nCác sản phẩm sát với câu hỏi của khách nhất ");
            sb.append("(chỉ được gợi ý các sản phẩm trong danh sách này, không bịa thêm tên). ");
            sb.append("Đây chỉ là phần trích, không phải toàn bộ kho: nếu khách hỏi món cửa hàng có bán ");
            sb.append("nhưng không thấy trong danh sách, hãy mời khách xem thêm ở trang sản phẩm ");
            sb.append("hoặc nói rõ hơn về nhu cầu để bạn tìm giúp.\n");
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
