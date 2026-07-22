package com.foodordering.order.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderDto {
    @NotNull(message = "User ID không được để trống")
    private Long userId;
    private String email;
    
    @NotEmpty(message = "Phải có ít nhất 1 món")
    @Valid
    private List<OrderItemRequest> items;
    
    private String deliveryAddress;
    private String phoneNumber;
    private String customerName;
    private String notes;
    private String paymentMethod;  // COD, SEPAY
    private Integer pointsToUse;
}
