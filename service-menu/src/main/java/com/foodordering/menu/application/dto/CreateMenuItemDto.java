package com.foodordering.menu.application.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * DTO để tạo MenuItem mới
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMenuItemDto {
    
    @NotNull(message = "Category ID không được để trống")
    private Long categoryId;
    
    @NotBlank(message = "Tên món ăn không được để trống")
    @Size(max = 200, message = "Tên không được vượt quá 200 ký tự")
    private String name;
    
    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;
    
    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    private BigDecimal price;
    
    private String imageUrl;
    
    private Integer displayOrder;
    
    private Boolean available;
}
