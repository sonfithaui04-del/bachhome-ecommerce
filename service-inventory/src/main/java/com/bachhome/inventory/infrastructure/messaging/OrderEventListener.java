package com.bachhome.inventory.infrastructure.messaging;

import com.bachhome.inventory.application.dto.DeductInventoryDto;
import com.bachhome.inventory.application.dto.event.OrderConfirmedEvent;
import com.bachhome.inventory.application.usecase.DeductInventoryUseCase;
import com.bachhome.inventory.domain.model.InventoryItem;
import com.bachhome.inventory.domain.repository.InventoryItemRepository;
import com.bachhome.inventory.infrastructure.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventListener {

    private final DeductInventoryUseCase deductInventoryUseCase;
    private final InventoryItemRepository inventoryItemRepository;

    @RabbitListener(queues = RabbitMQConfig.ORDER_INVENTORY_QUEUE)
    @Transactional
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        log.info("[INVENTORY-LISTENER] Received OrderConfirmedEvent for order: {}", event.getOrderId());

        try {
            List<DeductInventoryDto.InventoryDeductionDto> deductions = new ArrayList<>();

            for (OrderConfirmedEvent.OrderItemEventDto item : event.getItems()) {
                // Mỗi sản phẩm bán ra trừ thẳng số lượng tồn kho của chính sản phẩm đó
                Optional<InventoryItem> stock = inventoryItemRepository.findByProductId(item.getProductId());

                if (stock.isEmpty()) {
                    log.warn("[INVENTORY-LISTENER] Sản phẩm {} chưa gắn với mặt hàng trong kho, bỏ qua",
                            item.getProductId());
                    continue;
                }

                Long inventoryItemId = stock.get().getId();
                BigDecimal requiredQty = BigDecimal.valueOf(item.getQuantity());

                // Cộng dồn nếu sản phẩm xuất hiện nhiều lần trong cùng một đơn
                DeductInventoryDto.InventoryDeductionDto existing = deductions.stream()
                        .filter(d -> d.getInventoryItemId().equals(inventoryItemId))
                        .findFirst()
                        .orElse(null);

                if (existing != null) {
                    existing.setQuantity(existing.getQuantity().add(requiredQty));
                } else {
                    deductions.add(DeductInventoryDto.InventoryDeductionDto.builder()
                            .inventoryItemId(inventoryItemId)
                            .quantity(requiredQty)
                            .build());
                }
            }

            if (!deductions.isEmpty()) {
                DeductInventoryDto deductDto = DeductInventoryDto.builder()
                        .orderId(event.getOrderId())
                        .inventoryItems(deductions)
                        .build();

                deductInventoryUseCase.execute(deductDto);
                log.info("[INVENTORY-LISTENER] Successfully deducted inventory for order: {}", event.getOrderId());
            } else {
                log.warn("[INVENTORY-LISTENER] No inventory items to deduct for order: {}", event.getOrderId());
            }

        } catch (Exception e) {
            log.error("[INVENTORY-LISTENER] Failed to process order event: {}", e.getMessage());
            // TODO: Gửi sự kiện InventoryFailedEvent để Order Service biết mà rollback (Saga Pattern)
        }
    }
}
