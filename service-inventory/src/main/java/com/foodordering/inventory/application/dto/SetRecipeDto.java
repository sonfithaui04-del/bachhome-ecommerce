package com.foodordering.inventory.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO để set công thức cho món ăn
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetRecipeDto {
    
    @NotNull(message = "Menu Item ID không được để trống")
    private Long menuItemId;
    
    @NotNull(message = "Danh sách nguyên liệu không được để trống")
    private List<RecipeItemDto> ingredients;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipeItemDto {
        @NotNull(message = "Ingredient ID không được để trống")
        private Long ingredientId;
        
        @NotNull(message = "Số lượng không được để trống")
        @Positive(message = "Số lượng phải lớn hơn 0")
        private BigDecimal quantity;
        
        private String notes;
    }
}
