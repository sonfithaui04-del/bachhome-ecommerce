package com.foodordering.order.infrastructure.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

/**
 * Client để gọi Inventory Service
 */
@Service
@Slf4j
public class InventoryServiceClient {
    
    private final RestTemplate restTemplate;
    private final String inventoryServiceUrl;
    
    public InventoryServiceClient(
            RestTemplate restTemplate,
            @Value("${inventory.service.url:http://service-inventory:8085}") String inventoryServiceUrl) {
        this.restTemplate = restTemplate;
        this.inventoryServiceUrl = inventoryServiceUrl;
    }
    
    /**
     * Lấy công thức món ăn từ Inventory Service
     */
    public List<RecipeDto> getRecipeByMenuItemId(Long menuItemId) {
        try {
            String url = inventoryServiceUrl + "/recipes/menu-item/" + menuItemId;
            log.info("[INVENTORY-CLIENT] Getting recipe for menuItemId: {}", menuItemId);
            
            ResponseEntity<RecipeDto[]> response = restTemplate.getForEntity(url, RecipeDto[].class);
            return response.getBody() != null ? List.of(response.getBody()) : List.of();
        } catch (Exception e) {
            log.warn("[INVENTORY-CLIENT] Failed to get recipe for menuItemId {}: {}", menuItemId, e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Lấy thông tin nguyên liệu
     */
    public IngredientDto getIngredientById(Long ingredientId) {
        try {
            String url = inventoryServiceUrl + "/ingredients/" + ingredientId;
            return restTemplate.getForObject(url, IngredientDto.class);
        } catch (Exception e) {
            log.warn("[INVENTORY-CLIENT] Failed to get ingredient {}: {}", ingredientId, e.getMessage());
            return null;
        }
    }

    /**
     * Trừ nguyên liệu
     */
    public void deductInventory(Long orderId, List<IngredientDeductionDto> ingredients) {
        try {
            String url = inventoryServiceUrl + "/inventory/deduct";
            log.info("[INVENTORY-CLIENT] Deducting inventory for order: {}", orderId);
            
            DeductInventoryRequest request = DeductInventoryRequest.builder()
                    .orderId(orderId)
                    .ingredients(ingredients)
                    .build();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<DeductInventoryRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            log.info("[INVENTORY-CLIENT] Deduct response: {}", response.getBody());
        } catch (Exception e) {
            log.error("[INVENTORY-CLIENT] Failed to deduct inventory: {}", e.getMessage());
            throw new RuntimeException("Failed to deduct inventory: " + e.getMessage());
        }
    }
    
    /**
     * Hoàn lại nguyên liệu
     */
    public void restoreInventory(Long orderId, List<IngredientDeductionDto> ingredients) {
        try {
            String url = inventoryServiceUrl + "/inventory/restore";
            log.info("[INVENTORY-CLIENT] Restoring inventory for order: {}", orderId);
            
            DeductInventoryRequest request = DeductInventoryRequest.builder()
                    .orderId(orderId)
                    .ingredients(ingredients)
                    .build();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<DeductInventoryRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            log.info("[INVENTORY-CLIENT] Restore response: {}", response.getBody());
        } catch (Exception e) {
            log.error("[INVENTORY-CLIENT] Failed to restore inventory: {}", e.getMessage());
        }
    }
    
    // DTOs
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipeDto {
        private Long id;
        private Long menuItemId;
        private Long ingredientId;
        private String ingredientName;
        private BigDecimal quantity;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngredientDeductionDto {
        private Long ingredientId;
        private BigDecimal quantity;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeductInventoryRequest {
        private Long orderId;
        private List<IngredientDeductionDto> ingredients;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngredientDto {
        private Long id;
        private String name;
        private BigDecimal quantity;
    }
}
