package com.bachhome.order.infrastructure.client;

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
     * Lấy mặt hàng trong kho gắn với một sản phẩm đang bán.
     * Trả về null nếu sản phẩm chưa được gắn kho.
     */
    public InventoryItemDto getInventoryItemByProductId(Long productId) {
        try {
            String url = inventoryServiceUrl + "/inventory-items/by-product/" + productId;
            log.info("[INVENTORY-CLIENT] Getting stock for productId: {}", productId);
            return restTemplate.getForObject(url, InventoryItemDto.class);
        } catch (Exception e) {
            log.warn("[INVENTORY-CLIENT] Failed to get stock for productId {}: {}", productId, e.getMessage());
            return null;
        }
    }

    /**
     * Lấy thông tin một mặt hàng trong kho theo ID
     */
    public InventoryItemDto getInventoryItemById(Long inventoryItemId) {
        try {
            String url = inventoryServiceUrl + "/inventory-items/" + inventoryItemId;
            return restTemplate.getForObject(url, InventoryItemDto.class);
        } catch (Exception e) {
            log.warn("[INVENTORY-CLIENT] Failed to get inventory item {}: {}", inventoryItemId, e.getMessage());
            return null;
        }
    }

    /**
     * Trừ tồn kho
     */
    public void deductInventory(Long orderId, List<InventoryDeductionDto> inventoryItems) {
        try {
            String url = inventoryServiceUrl + "/inventory/deduct";
            log.info("[INVENTORY-CLIENT] Deducting inventory for order: {}", orderId);

            DeductInventoryRequest request = DeductInventoryRequest.builder()
                    .orderId(orderId)
                    .inventoryItems(inventoryItems)
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
     * Hoàn lại tồn kho
     */
    public void restoreInventory(Long orderId, List<InventoryDeductionDto> inventoryItems) {
        try {
            String url = inventoryServiceUrl + "/inventory/restore";
            log.info("[INVENTORY-CLIENT] Restoring inventory for order: {}", orderId);

            DeductInventoryRequest request = DeductInventoryRequest.builder()
                    .orderId(orderId)
                    .inventoryItems(inventoryItems)
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
    public static class InventoryDeductionDto {
        private Long inventoryItemId;
        private BigDecimal quantity;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeductInventoryRequest {
        private Long orderId;
        private List<InventoryDeductionDto> inventoryItems;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryItemDto {
        private Long id;
        private String name;
        private Long productId;
        private BigDecimal quantity;
    }
}
