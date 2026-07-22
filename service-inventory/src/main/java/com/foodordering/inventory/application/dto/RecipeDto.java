package com.foodordering.inventory.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO cho Recipe
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeDto {
    
    private Long id;
    private Long menuItemId;
    private Long ingredientId;
    private String ingredientName; // Tên nguyên liệu
    private String ingredientUnit; // Đơn vị
    private BigDecimal quantity; // Số lượng cần
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
