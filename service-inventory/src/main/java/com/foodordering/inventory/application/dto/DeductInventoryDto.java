package com.foodordering.inventory.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO cho việc trừ nguyên liệu khi confirm order
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeductInventoryDto {
    
    private Long orderId; // ID đơn hàng (để tracking)
    private List<IngredientDeductionDto> ingredients;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngredientDeductionDto {
        private Long ingredientId;
        private BigDecimal quantity; // Số lượng cần trừ
    }
}
