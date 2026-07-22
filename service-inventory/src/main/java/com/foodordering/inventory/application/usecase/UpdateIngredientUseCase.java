package com.foodordering.inventory.application.usecase;

import com.foodordering.inventory.application.dto.CreateIngredientDto;
import com.foodordering.inventory.application.dto.IngredientDto;
import com.foodordering.inventory.domain.model.Ingredient;
import com.foodordering.inventory.domain.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Cập nhật nguyên liệu
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateIngredientUseCase {
    
    private final IngredientRepository ingredientRepository;
    
    @Transactional
    public IngredientDto execute(Long id, CreateIngredientDto dto) {
        log.info("[UPDATE_INGREDIENT] Updating ingredient ID: {}", id);
        
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nguyên liệu với ID: " + id));
        
        ingredient.setName(dto.getName());
        ingredient.setUnit(dto.getUnit());
        ingredient.setQuantity(dto.getQuantity());
        ingredient.setMinQuantity(dto.getMinQuantity());
        ingredient.setCostPerUnit(dto.getCostPerUnit());
        ingredient.setExpiryDate(dto.getExpiryDate());
        ingredient.setDescription(dto.getDescription());
        if (dto.getActive() != null) {
            ingredient.setActive(dto.getActive());
        }
        
        Ingredient updated = ingredientRepository.save(ingredient);
        log.info("[UPDATE_INGREDIENT] Ingredient updated: {}", updated.getId());
        
        return toDto(updated);
    }
    
    private IngredientDto toDto(Ingredient ingredient) {
        return IngredientDto.builder()
                .id(ingredient.getId())
                .name(ingredient.getName())
                .unit(ingredient.getUnit())
                .quantity(ingredient.getQuantity())
                .minQuantity(ingredient.getMinQuantity())
                .costPerUnit(ingredient.getCostPerUnit())
                .expiryDate(ingredient.getExpiryDate())
                .description(ingredient.getDescription())
                .active(ingredient.getActive())
                .isLowStock(ingredient.isLowStock())
                .createdAt(ingredient.getCreatedAt())
                .updatedAt(ingredient.getUpdatedAt())
                .build();
    }
}
