package com.bachhome.ai.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Lấy danh sách sản phẩm đang bán từ service-product để:
 * 1) làm ngữ cảnh cho chatbot (chỉ tư vấn hàng thực tế),
 * 2) làm nguồn dữ liệu gợi ý sản phẩm kèm ảnh.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductContextProvider {

    private final RestTemplate loadBalancedRestTemplate;

    @Value("${ai.max-products:40}")
    private int maxProducts;

    /**
     * Lấy danh sách sản phẩm (đã giới hạn số lượng). Trả list rỗng nếu lỗi.
     */
    public List<Map<String, Object>> fetchProducts() {
        try {
            ResponseEntity<List<Map<String, Object>>> resp = loadBalancedRestTemplate.exchange(
                    "http://SERVICE-PRODUCT/products?availableOnly=true",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            List<Map<String, Object>> items = resp.getBody();
            if (items == null || items.isEmpty()) {
                return Collections.emptyList();
            }
            return items.stream().limit(maxProducts).collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("[AI] Không lấy được danh sách sản phẩm từ service-product: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Dựng chuỗi mô tả sản phẩm để đưa vào prompt.
     */
    public String buildContext(List<Map<String, Object>> items) {
        if (items == null || items.isEmpty()) {
            return "";
        }
        return items.stream()
                .map(this::formatItem)
                .collect(Collectors.joining("\n"));
    }

    private String formatItem(Map<String, Object> m) {
        Object name = m.get("name");
        Object price = m.get("price");
        Object category = m.get("categoryName");
        Object desc = m.get("description");
        StringBuilder sb = new StringBuilder("- ").append(name);
        if (category != null) {
            sb.append(" (danh mục: ").append(category);
            if (price != null) {
                sb.append(", giá: ").append(price).append(" VND");
            }
            sb.append(")");
        } else if (price != null) {
            sb.append(" (giá: ").append(price).append(" VND)");
        }
        if (desc != null && !desc.toString().isBlank()) {
            sb.append(": ").append(desc);
        }
        return sb.toString();
    }
}
