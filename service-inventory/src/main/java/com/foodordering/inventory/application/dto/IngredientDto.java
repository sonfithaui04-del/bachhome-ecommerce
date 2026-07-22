package com.foodordering.inventory.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO cho Ingredient
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredientDto {
    
    private Long id;
    private String name;
    private String unit;
    private BigDecimal quantity;
    private BigDecimal minQuantity;
    private BigDecimal costPerUnit;
    private LocalDate expiryDate;
    private String description;
    private Boolean active;
    private Boolean isLowStock; // Cảnh báo sắp hết
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
