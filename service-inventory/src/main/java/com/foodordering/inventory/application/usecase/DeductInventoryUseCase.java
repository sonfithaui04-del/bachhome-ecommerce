package com.foodordering.inventory.application.usecase;

import com.foodordering.inventory.application.dto.DeductInventoryDto;
import com.foodordering.inventory.domain.model.Ingredient;
import com.foodordering.inventory.domain.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Use Case: Trừ nguyên liệu khi confirm order
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeductInventoryUseCase {
    
    private final IngredientRepository ingredientRepository;
    
    @Transactional
    public void execute(DeductInventoryDto dto) {
        log.info("[DEDUCT_INVENTORY] Processing order ID: {}", dto.getOrderId());
        
        List<String> insufficientIngredients = new ArrayList<>();
        
        // 1. Kiểm tra tất cả nguyên liệu trước
        for (DeductInventoryDto.IngredientDeductionDto item : dto.getIngredients()) {
            Ingredient ingredient = ingredientRepository.findById(item.getIngredientId())
                    .orElseThrow(() -> new RuntimeException("Ingredient not found: " + item.getIngredientId()));
            
            if (ingredient.getQuantity().compareTo(item.getQuantity()) < 0) {
                insufficientIngredients.add(String.format("%s (Cần: %s %s, Còn: %s %s)", 
                    ingredient.getName(),
                    item.getQuantity(),
                    ingredient.getUnit(),
                    ingredient.getQuantity(),
                    ingredient.getUnit()
                ));
            }
        }
        
        // 2. Nếu có nguyên liệu không đủ -> throw exception
        if (!insufficientIngredients.isEmpty()) {
            String errorMsg = "Nguyên liệu không đủ: " + String.join(", ", insufficientIngredients);
            log.error("[DEDUCT_INVENTORY] {}", errorMsg);
            throw new RuntimeException(errorMsg);
        }
        
        // 3. Trừ nguyên liệu
        for (DeductInventoryDto.IngredientDeductionDto item : dto.getIngredients()) {
            Ingredient ingredient = ingredientRepository.findById(item.getIngredientId()).get();
            ingredient.deductQuantity(item.getQuantity());
            ingredientRepository.save(ingredient);
            
            log.info("[DEDUCT_INVENTORY] Deducted {} {} of {}", 
                item.getQuantity(), ingredient.getUnit(), ingredient.getName());
        }
        
        log.info("[DEDUCT_INVENTORY] Successfully deducted inventory for order {}", dto.getOrderId());
    }
}
