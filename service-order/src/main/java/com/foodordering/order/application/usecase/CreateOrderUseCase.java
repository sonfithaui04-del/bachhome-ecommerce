package com.foodordering.order.application.usecase;

import com.foodordering.order.application.dto.*;
import com.foodordering.order.domain.model.Order;
import com.foodordering.order.domain.model.OrderItem;
import com.foodordering.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateOrderUseCase {

    private final OrderRepository orderRepository;
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;
    private final com.foodordering.order.infrastructure.client.InventoryServiceClient inventoryServiceClient;
    private final com.foodordering.order.infrastructure.client.AuthServiceClient authServiceClient;

    @Transactional
    public OrderDto execute(CreateOrderDto request) {
        log.info("[CREATE_ORDER] Creating order for user: {}", request.getUserId());
        
        Order order = Order.builder()
                .userId(request.getUserId())
                .email(request.getEmail())
                .customerName(request.getCustomerName())
                .deliveryAddress(request.getDeliveryAddress())
                .phoneNumber(request.getPhoneNumber())
                .notes(request.getNotes())
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "COD")
                .paymentStatus("PENDING")
                .pointsUsed(request.getPointsToUse() != null ? request.getPointsToUse() : 0)
                .build();
        
        for (OrderItemRequest itemReq : request.getItems()) {
            OrderItem item = OrderItem.builder()
                    .menuItemId(itemReq.getMenuItemId())
                    .menuItemName(itemReq.getMenuItemName() != null ? itemReq.getMenuItemName() : "Item " + itemReq.getMenuItemId())
                    .quantity(itemReq.getQuantity())
                    .price(itemReq.getPrice() != null ? itemReq.getPrice() : BigDecimal.valueOf(50000))
                    .imageUrl(itemReq.getImageUrl())
                    .build();
            
            // Calculate subtotal manually as @PrePersist hasn't run yet
            item.setSubtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            
            order.addItem(item);
            
            // Check Inventory
            checkInventory(item);
        }
        
        Order savedOrder = orderRepository.save(order);
        log.info("[CREATE_ORDER] Order created with ID: {}", savedOrder.getId());
        
        // Deduct points
        if (savedOrder.getPointsUsed() != null && savedOrder.getPointsUsed() > 0) {
            authServiceClient.deductPoints(savedOrder.getUserId(), savedOrder.getPointsUsed());
            log.info("[CREATE_ORDER] Deducted {} points for User {}", savedOrder.getPointsUsed(), savedOrder.getUserId());
        }
        
        // Publish event to RabbitMQ for Socket Notification
        try {
            java.util.Map<String, Object> event = new java.util.HashMap<>();
            event.put("type", "ORDER_CREATED");
            event.put("orderId", savedOrder.getId());
            event.put("userId", savedOrder.getUserId());
            event.put("email", savedOrder.getEmail());
            event.put("customerName", savedOrder.getCustomerName());
            event.put("totalAmount", savedOrder.getTotalAmount());
            event.put("createdAt", savedOrder.getCreatedAt() != null ? savedOrder.getCreatedAt().toString() : null);
            
            rabbitTemplate.convertAndSend("food-ordering-exchange", "order.confirmed", event);
            log.info("[CREATE_ORDER] Published OrderCreatedEvent for Order ID: {} with customerName: {}", savedOrder.getId(), savedOrder.getCustomerName());
        } catch (Exception e) {
            log.error("[CREATE_ORDER] Failed to publish event", e);
        }
        
        return toDto(savedOrder);
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
                .pointsUsed(order.getPointsUsed())
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

    private void checkInventory(OrderItem item) {
        try {
            java.util.List<com.foodordering.order.infrastructure.client.InventoryServiceClient.RecipeDto> recipes = inventoryServiceClient.getRecipeByMenuItemId(item.getMenuItemId());
            
            for (com.foodordering.order.infrastructure.client.InventoryServiceClient.RecipeDto recipe : recipes) {
                BigDecimal requiredQty = recipe.getQuantity().multiply(BigDecimal.valueOf(item.getQuantity()));
                
                com.foodordering.order.infrastructure.client.InventoryServiceClient.IngredientDto ingredient = inventoryServiceClient.getIngredientById(recipe.getIngredientId());
                
                if (ingredient == null) {
                    log.warn("Ingredient {} not found, skipping check", recipe.getIngredientId());
                    continue;
                }
                
                if (ingredient.getQuantity().compareTo(requiredQty) < 0) {
                    throw new RuntimeException("Insufficient inventory for item: " + item.getMenuItemName() + 
                        " (Required: " + requiredQty + " " + ingredient.getName() + 
                        ", Available: " + ingredient.getQuantity() + ")");
                }
            }
        } catch (Exception e) {
            log.error("Inventory check failed: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
}
