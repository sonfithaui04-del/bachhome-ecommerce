package com.bachhome.order.application.usecase;

import com.bachhome.order.application.dto.*;
import com.bachhome.order.domain.model.Order;
import com.bachhome.order.domain.model.OrderItem;
import com.bachhome.order.domain.repository.OrderRepository;
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
    private final com.bachhome.order.infrastructure.client.InventoryServiceClient inventoryServiceClient;
    private final com.bachhome.order.infrastructure.client.AuthServiceClient authServiceClient;

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
                    .productId(itemReq.getProductId())
                    .productName(itemReq.getProductName() != null ? itemReq.getProductName() : "Item " + itemReq.getProductId())
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
            
            rabbitTemplate.convertAndSend("bachhome-exchange", "order.confirmed", event);
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
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .subtotal(item.getSubtotal())
                .imageUrl(item.getImageUrl())
                .build();
    }

    private void checkInventory(OrderItem item) {
        try {
            com.bachhome.order.infrastructure.client.InventoryServiceClient.InventoryItemDto stock =
                    inventoryServiceClient.getInventoryItemByProductId(item.getProductId());

            // Sản phẩm chưa được gắn với mặt hàng trong kho thì bỏ qua kiểm tra
            if (stock == null || stock.getQuantity() == null) {
                log.warn("Sản phẩm {} chưa gắn tồn kho, bỏ qua kiểm tra", item.getProductId());
                return;
            }

            BigDecimal requiredQty = BigDecimal.valueOf(item.getQuantity());

            if (stock.getQuantity().compareTo(requiredQty) < 0) {
                throw new RuntimeException("Insufficient inventory for item: " + item.getProductName() +
                    " (Required: " + requiredQty +
                    ", Available: " + stock.getQuantity() + ")");
            }
        } catch (Exception e) {
            log.error("Inventory check failed: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
}
