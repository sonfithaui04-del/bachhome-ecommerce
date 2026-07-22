package com.foodordering.inventory.application.usecase;

import com.foodordering.inventory.application.dto.DeductInventoryDto;
import com.foodordering.inventory.domain.model.Ingredient;
import com.foodordering.inventory.domain.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Hoàn lại nguyên liệu khi cancel order
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RestoreInventoryUseCase {
    
    private final IngredientRepository ingredientRepository;
    
    @Transactional
    public void execute(DeductInventoryDto dto) {
        log.info("[RESTORE_INVENTORY] Restoring inventory for order ID: {}", dto.getOrderId());
        
        for (DeductInventoryDto.IngredientDeductionDto item : dto.getIngredients()) {
            Ingredient ingredient = ingredientRepository.findById(item.getIngredientId())
                    .orElseThrow(() -> new RuntimeException("Ingredient not found: " + item.getIngredientId()));
            
            ingredient.addQuantity(item.getQuantity());
            ingredientRepository.save(ingredient);
            
            log.info("[RESTORE_INVENTORY] Restored {} {} of {}", 
                item.getQuantity(), ingredient.getUnit(), ingredient.getName());
        }
        
        log.info("[RESTORE_INVENTORY] Successfully restored inventory for order {}", dto.getOrderId());
    }
}
