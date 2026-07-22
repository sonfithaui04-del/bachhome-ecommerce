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
 * Use Case: Tạo nguyên liệu mới
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreateIngredientUseCase {
    
    private final IngredientRepository ingredientRepository;
    
    @Transactional
    public IngredientDto execute(CreateIngredientDto dto) {
        log.info("[CREATE_INGREDIENT] Creating ingredient: {}", dto.getName());
        
        // Kiểm tra tên đã tồn tại chưa
        if (ingredientRepository.findByName(dto.getName()).isPresent()) {
            throw new RuntimeException("Nguyên liệu với tên này đã tồn tại");
        }
        
        Ingredient ingredient = Ingredient.builder()
                .name(dto.getName())
                .unit(dto.getUnit())
                .quantity(dto.getQuantity())
                .minQuantity(dto.getMinQuantity())
                .costPerUnit(dto.getCostPerUnit())
                .expiryDate(dto.getExpiryDate())
                .description(dto.getDescription())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();
        
        Ingredient saved = ingredientRepository.save(ingredient);
        log.info("[CREATE_INGREDIENT] Ingredient created with ID: {}", saved.getId());
        
        return toDto(saved);
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
