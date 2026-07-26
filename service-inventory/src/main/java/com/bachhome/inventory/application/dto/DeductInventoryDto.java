package com.bachhome.inventory.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO cho việc trừ mặt hàng trong kho khi confirm order
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeductInventoryDto {
    
    private Long orderId; // ID đơn hàng (để tracking)
    private List<InventoryDeductionDto> inventoryItems;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryDeductionDto {
        private Long inventoryItemId;
        private BigDecimal quantity; // Số lượng cần trừ
    }
}
