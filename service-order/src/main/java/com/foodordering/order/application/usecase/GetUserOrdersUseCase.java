package com.foodordering.order.application.usecase;

import com.foodordering.order.application.dto.OrderDto;
import com.foodordering.order.application.dto.OrderItemDto;
import com.foodordering.order.domain.model.Order;
import com.foodordering.order.domain.model.OrderItem;
import com.foodordering.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetUserOrdersUseCase {

    private final OrderRepository orderRepository;

    public List<OrderDto> execute(Long userId) {
        log.info("[GET_USER_ORDERS] Fetching orders for user: {}", userId);
        
        List<Order> orders = orderRepository.findByUserId(userId);
        log.info("[GET_USER_ORDERS] Found {} orders", orders.size());
        
        return orders.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

   private OrderDto toDto(Order order) {
        return OrderDto.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .deliveryAddress(order.getDeliveryAddress())
                .phoneNumber(order.getPhoneNumber())
                .notes(order.getNotes())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .items(order.getItems().stream().map(this::toItemDto).collect(Collectors.toList()))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderItemDto toItemDto(OrderItem item) {
        return OrderItemDto.builder()
                .menuItemId(item.getMenuItemId())
                .menuItemName(item.getMenuItemName())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .subtotal(item.getSubtotal())
                .imageUrl(item.getImageUrl())
                .build();
    }
}
