package com.foodordering.order.infrastructure.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

    public void deductPoints(Long userId, Integer points) {
        if (userId == null || points == null || points <= 0) return;
        try {
            String url = authServiceUrl + "/users/" + userId + "/points/deduct";
            log.info("[AUTH-CLIENT] Deducting {} points for user: {}", points, userId);
            
            UpdatePointsDto request = new UpdatePointsDto(points);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<UpdatePointsDto> entity = new HttpEntity<>(request, headers);
            
            restTemplate.postForEntity(url, entity, String.class);
        } catch (Exception e) {
            log.error("[AUTH-CLIENT] Failed to deduct points: {}", e.getMessage());
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdatePointsDto {
        private Integer points;
    }
}
