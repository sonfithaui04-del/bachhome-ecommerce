package com.bachhome.product.application.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * DTO để tạo Product mới
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductDto {
    
    @NotNull(message = "Category ID không được để trống")
    private Long categoryId;
    
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 200, message = "Tên không được vượt quá 200 ký tự")
    private String name;
    
    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;
    
    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    private BigDecimal price;
    
    private String imageUrl;
    
    private String brand;
    
    private String origin;
    
    private Integer warrantyMonths;
    
    private String material;
    
    private String specification;
    
    private Integer displayOrder;
    
    private Boolean available;
}
