package com.foodordering.inventory.application.usecase;

import com.foodordering.inventory.application.dto.IngredientDto;
import com.foodordering.inventory.domain.model.Ingredient;
import com.foodordering.inventory.domain.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Use Case: Lấy tất cả nguyên liệu
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetAllIngredientsUseCase {
    
    private final IngredientRepository ingredientRepository;
    
    @Transactional(readOnly = true)
    public List<IngredientDto> execute(boolean activeOnly) {
        log.info("[GET_ALL_INGREDIENTS] Fetching ingredients, activeOnly: {}", activeOnly);
        
        List<Ingredient> ingredients = activeOnly 
                ? ingredientRepository.findByActiveTrue()
                : ingredientRepository.findAll();
        
        log.info("[GET_ALL_INGREDIENTS] Found {} ingredients", ingredients.size());
        
        return ingredients.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
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
