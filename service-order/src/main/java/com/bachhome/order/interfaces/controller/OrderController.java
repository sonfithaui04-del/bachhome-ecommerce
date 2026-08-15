package com.bachhome.order.interfaces.controller;

import com.bachhome.order.application.dto.CreateOrderDto;
import com.bachhome.order.application.dto.OrderDto;
import com.bachhome.order.application.dto.OrderItemDto;
import com.bachhome.order.application.usecase.CreateOrderUseCase;
import com.bachhome.order.application.usecase.GetUserOrdersUseCase;
import com.bachhome.order.domain.model.Order;
import com.bachhome.order.domain.model.OrderItem;
import com.bachhome.order.domain.model.OrderStatus;
import com.bachhome.order.domain.repository.OrderRepository;
import com.bachhome.order.infrastructure.client.InventoryServiceClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Orders", description = "Order Management APIs")
public class OrderController {
    
    private final CreateOrderUseCase createOrderUseCase;
    private final GetUserOrdersUseCase getUserOrdersUseCase;
    private final OrderRepository orderRepository;
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;
    private final com.bachhome.order.infrastructure.config.RabbitMQConfig rabbitMQConfig;
    private final InventoryServiceClient inventoryServiceClient;

    @PostMapping
    @Operation(summary = "Tạo đơn hàng mới")
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody CreateOrderDto request) {
        log.info("[ORDER-CONTROLLER] Create order for user: {}", request.getUserId());
        OrderDto order = createOrderUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping
    @Operation(summary = "Lấy tất cả đơn hàng (Admin)")
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        log.info("[ORDER-CONTROLLER] Get all orders");
        List<Order> orders = orderRepository.findAll();
        List<OrderDto> orderDtos = orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderDtos);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Lấy đơn hàng của user")
    public ResponseEntity<List<OrderDto>> getUserOrders(@PathVariable Long userId) {
        log.info("[ORDER-CONTROLLER] Get orders for user: {}", userId);
        List<OrderDto> orders = getUserOrdersUseCase.execute(userId);
        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/{orderId}/status")
    @Operation(summary = "Cập nhật trạng thái đơn hàng (Admin)")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> payload) {
        log.info("[ORDER-CONTROLLER] Update order {} status to {}", orderId, payload.get("status"));
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        OrderStatus oldStatus = order.getStatus();
        OrderStatus newStatus = OrderStatus.valueOf(payload.get("status"));
        
        // SEPAY: Bắt buộc thanh toán trước khi chuyển sang PREPARING trở đi
        if ("SEPAY".equals(order.getPaymentMethod()) && !"SUCCESS".equals(order.getPaymentStatus())) {
            // Chỉ cho phép CONFIRMED và CANCELLED nếu chưa thanh toán
            if (newStatus == OrderStatus.PREPARING || newStatus == OrderStatus.DELIVERING || newStatus == OrderStatus.COMPLETED) {
                log.warn("[ORDER-CONTROLLER] Order {} - SEPAY unpaid, blocking status change to {}", orderId, newStatus);
                throw new RuntimeException("Đơn hàng chuyển khoản chưa được thanh toán. Vui lòng đợi khách hàng thanh toán trước khi xử lý.");
            }
        }
        
        // Logic trừ/hoàn tồn kho (Event-Driven)
        if (oldStatus == OrderStatus.PENDING && newStatus == OrderStatus.CONFIRMED) {
            // PENDING -> CONFIRMED: Gửi sự kiện để trừ tồn kho (Async)
            log.info("[ORDER-CONTROLLER] Confirming order {} - publishing OrderConfirmedEvent", orderId);
            
            com.bachhome.order.application.dto.event.OrderConfirmedEvent event = com.bachhome.order.application.dto.event.OrderConfirmedEvent.builder()
                    .orderId(order.getId())
                    .userId(order.getUserId())
                    .email(order.getEmail())
                    .items(order.getItems().stream()
                            .map(item -> com.bachhome.order.application.dto.event.OrderConfirmedEvent.OrderItemEventDto.builder()
                                    .productId(item.getProductId())
                                    .quantity(item.getQuantity())
                                    .build())
                            .collect(Collectors.toList()))
                    .build();
            
            rabbitTemplate.convertAndSend(
                com.bachhome.order.infrastructure.config.RabbitMQConfig.EXCHANGE, 
                com.bachhome.order.infrastructure.config.RabbitMQConfig.ORDER_CONFIRMED_ROUTING_KEY, 
                event
            );
            
        } else if (newStatus == OrderStatus.CANCELLED && isInventoryDeducted(oldStatus)) {
            // Huỷ đơn sau khi đã trừ kho (CONFIRMED trở đi) -> hoàn lại tồn kho
            log.info("[ORDER-CONTROLLER] Cancelling order {} from {} - restoring inventory", orderId, oldStatus);
            try {
                restoreInventoryForOrder(order);
            } catch (Exception e) {
                log.error("[ORDER-CONTROLLER] Restore inventory failed for order {}: {}", orderId, e.getMessage());
            }
        }
        
        // COD: Auto-mark as paid when order is completed
        if (newStatus == OrderStatus.COMPLETED && "COD".equals(order.getPaymentMethod())) {
            order.setPaymentStatus("SUCCESS");
            log.info("[ORDER-CONTROLLER] COD Order {} completed - auto-marked as paid", orderId);
        }
        
        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);
        
        // Publish OrderStatusChangedEvent for Realtime Updates (User & Admin)
        try {
            Map<String, Object> statusEvent = new java.util.HashMap<>();
            statusEvent.put("type", "ORDER_STATUS_CHANGED");
            statusEvent.put("orderId", updated.getId());
            statusEvent.put("userId", updated.getUserId());
            statusEvent.put("status", updated.getStatus().name());
            statusEvent.put("updatedAt", updated.getUpdatedAt().toString());
            
            rabbitTemplate.convertAndSend(
                com.bachhome.order.infrastructure.config.RabbitMQConfig.EXCHANGE, 
                "order.status.changed", 
                statusEvent
            );
            log.info("[ORDER-CONTROLLER] Published order.status.changed event for Order {}", updated.getId());
        } catch (Exception e) {
            log.error("[ORDER-CONTROLLER] Failed to publish status change event", e);
        }
        
        return ResponseEntity.ok(convertToDto(updated));
    }
    
    /**
     * Tồn kho bị trừ khi đơn chuyển sang CONFIRMED (qua OrderConfirmedEvent).
     * Vì vậy huỷ đơn từ CONFIRMED trở đi thì phải hoàn kho, còn huỷ khi PENDING thì không.
     */
    private boolean isInventoryDeducted(OrderStatus status) {
        return status == OrderStatus.CONFIRMED
                || status == OrderStatus.PREPARING
                || status == OrderStatus.READY
                || status == OrderStatus.DELIVERING;
    }

    /**
     * Trừ tồn kho cho đơn hàng
     */
    private void deductInventoryForOrder(Order order) {
        List<InventoryServiceClient.InventoryDeductionDto> totalDeductions = new java.util.ArrayList<>();
        
        for (OrderItem item : order.getItems()) {
            // Mỗi sản phẩm bán ra trừ thẳng tồn kho của chính sản phẩm đó
            InventoryServiceClient.InventoryItemDto stock =
                    inventoryServiceClient.getInventoryItemByProductId(item.getProductId());

            if (stock == null) {
                log.warn("Sản phẩm {} chưa gắn tồn kho, bỏ qua trừ kho", item.getProductId());
                continue;
            }

            java.math.BigDecimal requiredQuantity = java.math.BigDecimal.valueOf(item.getQuantity());

            // Cộng dồn nếu sản phẩm xuất hiện nhiều lần trong cùng một đơn
            InventoryServiceClient.InventoryDeductionDto existing = totalDeductions.stream()
                    .filter(d -> d.getInventoryItemId().equals(stock.getId()))
                    .findFirst()
                    .orElse(null);

            if (existing != null) {
                existing.setQuantity(existing.getQuantity().add(requiredQuantity));
            } else {
                totalDeductions.add(InventoryServiceClient.InventoryDeductionDto.builder()
                        .inventoryItemId(stock.getId())
                        .quantity(requiredQuantity)
                        .build());
            }
        }

        if (!totalDeductions.isEmpty()) {
            inventoryServiceClient.deductInventory(order.getId(), totalDeductions);
        }
    }
    
    @PutMapping("/{orderId}/confirm-receipt")
    @Operation(summary = "Người dùng xác nhận đã nhận hàng")
    public ResponseEntity<OrderDto> confirmReceipt(@PathVariable Long orderId) {
        log.info("[ORDER-CONTROLLER] User confirming receipt for order {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        
        // Cho phép xác nhận khi trạng thái là DELIVERING
        if (order.getStatus() != OrderStatus.DELIVERING) {
             throw new RuntimeException("Chỉ có thể xác nhận nhận hàng khi đơn hàng đang được giao (Delivering).");
        }
        
        order.setStatus(OrderStatus.COMPLETED);
        
        // COD: Tự động cập nhật thanh toán thành công
        if ("COD".equals(order.getPaymentMethod())) {
            order.setPaymentStatus("SUCCESS");
        }
        
        Order updated = orderRepository.save(order);
        
        // Gửi sự kiện realtime
        try {
            Map<String, Object> statusEvent = new java.util.HashMap<>();
            statusEvent.put("type", "ORDER_STATUS_CHANGED");
            statusEvent.put("orderId", updated.getId());
            statusEvent.put("userId", updated.getUserId());
            statusEvent.put("status", updated.getStatus().name());
            statusEvent.put("updatedAt", updated.getUpdatedAt().toString());
            
            rabbitTemplate.convertAndSend(
                com.bachhome.order.infrastructure.config.RabbitMQConfig.EXCHANGE, 
                "order.status.changed", 
                statusEvent
            );
            log.info("[ORDER-CONTROLLER] Published order.status.changed event for Order {}", updated.getId());
        } catch (Exception e) {
            log.error("[ORDER-CONTROLLER] Failed to publish status change event", e);
        }
        
        return ResponseEntity.ok(convertToDto(updated));
    }

    @PostMapping("/{orderId}/cancel-order")
    @Operation(summary = "Người dùng hủy đơn hàng")
    public ResponseEntity<OrderDto> cancelOrder(@PathVariable Long orderId) {
        log.info("[ORDER-CONTROLLER] User cancelling order {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        
        // Chỉ cho phép hủy khi PENDING hoặc CONFIRMED
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
             throw new RuntimeException("Không thể hủy đơn hàng khi đã bắt đầu chuẩn bị hoặc đang giao.");
        }

        // Đã trừ kho từ lúc xác nhận đơn -> hoàn lại tồn kho trước khi huỷ
        if (isInventoryDeducted(order.getStatus())) {
            log.info("[ORDER-CONTROLLER] Cancelling order {} from {} - restoring inventory", orderId, order.getStatus());
            try {
                restoreInventoryForOrder(order);
            } catch (Exception e) {
                log.error("[ORDER-CONTROLLER] Restore inventory failed for order {}: {}", orderId, e.getMessage());
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order updated = orderRepository.save(order);
        
        // Gửi sự kiện realtime
        try {
            Map<String, Object> statusEvent = new java.util.HashMap<>();
            statusEvent.put("type", "ORDER_STATUS_CHANGED");
            statusEvent.put("orderId", updated.getId());
            statusEvent.put("userId", updated.getUserId());
            statusEvent.put("status", updated.getStatus().name());
            statusEvent.put("updatedAt", updated.getUpdatedAt().toString());
            
            rabbitTemplate.convertAndSend(
                com.bachhome.order.infrastructure.config.RabbitMQConfig.EXCHANGE, 
                "order.status.changed", 
                statusEvent
            );
        } catch (Exception e) {
            log.error("Failed to publish status change event", e);
        }
        
        return ResponseEntity.ok(convertToDto(updated));
    }

    /**
     * Hoàn lại tồn kho cho đơn hàng
     */
    private void restoreInventoryForOrder(Order order) {
        List<InventoryServiceClient.InventoryDeductionDto> totalDeductions = new java.util.ArrayList<>();

        for (OrderItem item : order.getItems()) {
            InventoryServiceClient.InventoryItemDto stock =
                    inventoryServiceClient.getInventoryItemByProductId(item.getProductId());

            if (stock == null) {
                log.warn("Sản phẩm {} chưa gắn tồn kho, bỏ qua hoàn kho", item.getProductId());
                continue;
            }

            java.math.BigDecimal requiredQuantity = java.math.BigDecimal.valueOf(item.getQuantity());

            InventoryServiceClient.InventoryDeductionDto existing = totalDeductions.stream()
                    .filter(d -> d.getInventoryItemId().equals(stock.getId()))
                    .findFirst()
                    .orElse(null);

            if (existing != null) {
                existing.setQuantity(existing.getQuantity().add(requiredQuantity));
            } else {
                totalDeductions.add(InventoryServiceClient.InventoryDeductionDto.builder()
                        .inventoryItemId(stock.getId())
                        .quantity(requiredQuantity)
                        .build());
            }
        }

        if (!totalDeductions.isEmpty()) {
            inventoryServiceClient.restoreInventory(order.getId(), totalDeductions);
        }
    }

    // --- Shipper Endpoints ---

    @GetMapping("/shipper/ready")
    @Operation(summary = "Lấy danh sách đơn hàng sẵn sàng giao (Cho Shipper)")
    public ResponseEntity<List<OrderDto>> getOrdersReadyForDelivery() {
        log.info("[ORDER-CONTROLLER] Shipper fetching ready orders");
        List<Order> orders = orderRepository.findByStatus(OrderStatus.READY);
        return ResponseEntity.ok(orders.stream().map(this::convertToDto).collect(Collectors.toList()));
    }

    @GetMapping("/shipper/current/{shipperId}")
    @Operation(summary = "Lấy danh sách đơn hàng đang giao của Shipper")
    public ResponseEntity<List<OrderDto>> getShipperCurrentOrders(@PathVariable Long shipperId) {
        log.info("[ORDER-CONTROLLER] Fetching current orders for shipper {}", shipperId);
        List<Order> orders = orderRepository.findByShipperId(shipperId).stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERING)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orders.stream().map(this::convertToDto).collect(Collectors.toList()));
    }

    @PutMapping("/{orderId}/pickup")
    @Operation(summary = "Shipper nhận đơn hàng để đi giao")
    public ResponseEntity<OrderDto> pickUpOrder(@PathVariable Long orderId, @RequestParam Long shipperId) {
        log.info("[ORDER-CONTROLLER] Shipper {} picking up order {}", shipperId, orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (order.getStatus() != OrderStatus.READY) {
            throw new RuntimeException("Đơn hàng không ở trạng thái sẵn sàng để nhận.");
        }

        order.setStatus(OrderStatus.DELIVERING);
        order.setShipperId(shipperId);
        order.setAssignedAt(java.time.LocalDateTime.now());
        Order updated = orderRepository.save(order);

        publishStatusChangeEvent(updated);
        return ResponseEntity.ok(convertToDto(updated));
    }

    @PutMapping("/{orderId}/deliver")
    @Operation(summary = "Shipper xác nhận đã giao hàng thành công")
    public ResponseEntity<OrderDto> deliverOrder(@PathVariable Long orderId) {
        log.info("[ORDER-CONTROLLER] Shipper confirming delivery for order {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (order.getStatus() != OrderStatus.DELIVERING) {
            throw new RuntimeException("Đơn hàng không ở trạng thái đang giao.");
        }

        order.setStatus(OrderStatus.COMPLETED);
        // Nếu thanh toán COD thì đánh dấu là thành công luôn
        if ("COD".equals(order.getPaymentMethod())) {
            order.setPaymentStatus("SUCCESS");
        }
        Order updated = orderRepository.save(order);

        publishStatusChangeEvent(updated);
        return ResponseEntity.ok(convertToDto(updated));
    }

    private void publishStatusChangeEvent(Order updated) {
        try {
            Map<String, Object> statusEvent = new java.util.HashMap<>();
            statusEvent.put("type", "ORDER_STATUS_CHANGED");
            statusEvent.put("orderId", updated.getId());
            statusEvent.put("userId", updated.getUserId());
            statusEvent.put("status", updated.getStatus().name());
            statusEvent.put("updatedAt", updated.getUpdatedAt().toString());
            
            rabbitTemplate.convertAndSend(
                com.bachhome.order.infrastructure.config.RabbitMQConfig.EXCHANGE, 
                "order.status.changed", 
                statusEvent
            );
        } catch (Exception e) {
            log.error("Failed to publish status change event", e);
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Health Check")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Order Service is running");
    }

    private OrderDto convertToDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        
        if (order.getItems() != null) {
            List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(item -> OrderItemDto.builder()
                    .productId(item.getProductId())
                    .productName(item.getProductName())
                    .quantity(item.getQuantity())
                    .price(item.getPrice())
                    .subtotal(item.getSubtotal())
                    .imageUrl(item.getImageUrl())
                    .build())
                .collect(Collectors.toList());
            dto.setItems(itemDtos);
        }
        
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus().name());
        dto.setDeliveryAddress(order.getDeliveryAddress());
        dto.setPhoneNumber(order.getPhoneNumber());
        dto.setNotes(order.getNotes());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setShipperId(order.getShipperId());
        dto.setAssignedAt(order.getAssignedAt());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        return dto;
    }
}
