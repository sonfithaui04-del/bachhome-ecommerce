package com.bachhome.product.infrastructure.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Client để gọi Auth Service - lấy thông tin người dùng cho phần đánh giá sản phẩm
 */
@Service
@Slf4j
public class AuthServiceClient {

    private final RestTemplate restTemplate;
    private final String authServiceUrl;

    public AuthServiceClient(
            RestTemplate restTemplate,
            @Value("${auth.service.url:http://service-auth:8081}") String authServiceUrl) {
        this.restTemplate = restTemplate;
        this.authServiceUrl = authServiceUrl;
    }

    /**
     * Lấy tên hiển thị của người dùng. Trả về null nếu không lấy được
     * để phía gọi tự quyết định cách hiển thị thay thế.
     */
    public String getDisplayName(Long userId) {
        if (userId == null) return null;
        try {
            String url = authServiceUrl + "/users/" + userId;
            UserDto user = restTemplate.getForObject(url, UserDto.class);
            if (user == null) return null;

            if (user.getFullName() != null && !user.getFullName().isBlank()) {
                return user.getFullName();
            }
            // Không có họ tên thì lấy phần trước @ của email làm tên đăng nhập
            if (user.getEmail() != null && user.getEmail().contains("@")) {
                return user.getEmail().substring(0, user.getEmail().indexOf('@'));
            }
            return user.getEmail();
        } catch (Exception e) {
            log.warn("[AUTH-CLIENT] Không lấy được tên người dùng {}: {}", userId, e.getMessage());
            return null;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDto {
        private Long id;
        private String email;
        private String fullName;
        private String phoneNumber;
        private String role;
        private Integer loyaltyPoints;
    }
}
