package com.foodordering.inventory.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO để tạo Ingredient mới
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateIngredientDto {
    
    @NotBlank(message = "Tên nguyên liệu không được để trống")
    private String name;
    
    @NotBlank(message = "Đơn vị không được để trống")
    private String unit;
    
    @NotNull(message = "Số lượng không được để trống")
    @Positive(message = "Số lượng phải lớn hơn 0")
    private BigDecimal quantity;
    
    private BigDecimal minQuantity;
    
    private BigDecimal costPerUnit;
    
    private LocalDate expiryDate;
    
    private String description;
    
    private Boolean active;
}
