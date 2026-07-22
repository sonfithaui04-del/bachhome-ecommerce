package com.foodordering.inventory.infrastructure.messaging;

import com.foodordering.inventory.application.dto.DeductInventoryDto;
import com.foodordering.inventory.application.dto.event.OrderConfirmedEvent;
import com.foodordering.inventory.application.usecase.DeductInventoryUseCase;
import com.foodordering.inventory.domain.model.Recipe;
import com.foodordering.inventory.domain.repository.RecipeRepository;
import com.foodordering.inventory.infrastructure.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventListener {

    private final DeductInventoryUseCase deductInventoryUseCase;
    private final RecipeRepository recipeRepository;

    @RabbitListener(queues = RabbitMQConfig.ORDER_INVENTORY_QUEUE)
    @Transactional
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        log.info("[INVENTORY-LISTENER] Received OrderConfirmedEvent for order: {}", event.getOrderId());

        try {
            List<DeductInventoryDto.IngredientDeductionDto> deductions = new ArrayList<>();

            for (OrderConfirmedEvent.OrderItemEventDto item : event.getItems()) {
                // Tìm công thức cho món ăn
                List<Recipe> recipes = recipeRepository.findByMenuItemId(item.getMenuItemId());
                
                for (Recipe recipe : recipes) {
                    BigDecimal requiredQty = recipe.getQuantity().multiply(BigDecimal.valueOf(item.getQuantity()));
                    
                    // Cộng dồn nếu đã có
                    DeductInventoryDto.IngredientDeductionDto existing = deductions.stream()
                            .filter(d -> d.getIngredientId().equals(recipe.getIngredient().getId()))
                            .findFirst()
                            .orElse(null);
                            
                    if (existing != null) {
                        existing.setQuantity(existing.getQuantity().add(requiredQty));
                    } else {
                        deductions.add(DeductInventoryDto.IngredientDeductionDto.builder()
                                .ingredientId(recipe.getIngredient().getId())
                                .quantity(requiredQty)
                                .build());
                    }
                }
            }
            
            if (!deductions.isEmpty()) {
                DeductInventoryDto deductDto = DeductInventoryDto.builder()
                        .orderId(event.getOrderId())
                        .ingredients(deductions)
                        .build();
                
                deductInventoryUseCase.execute(deductDto);
                log.info("[INVENTORY-LISTENER] Successfully deducted inventory for order: {}", event.getOrderId());
            } else {
                log.warn("[INVENTORY-LISTENER] No ingredients to deduct for order: {}", event.getOrderId());
            }

        } catch (Exception e) {
            log.error("[INVENTORY-LISTENER] Failed to process order event: {}", e.getMessage());
            // TODO: Gửi sự kiện InventoryFailedEvent để Order Service biết mà rollback (Saga Pattern)
        }
    }
}
