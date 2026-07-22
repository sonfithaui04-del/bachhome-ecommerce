package com.foodordering.inventory.application.usecase;

import com.foodordering.inventory.application.dto.RecipeDto;
import com.foodordering.inventory.domain.model.Recipe;
import com.foodordering.inventory.domain.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Use Case: Lấy công thức của món ăn
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetRecipeByMenuItemIdUseCase {
    
    private final RecipeRepository recipeRepository;
    
    @Transactional(readOnly = true)
    public List<RecipeDto> execute(Long menuItemId) {
        log.info("[GET_RECIPE] Getting recipe for menuItemId: {}", menuItemId);
        
        List<Recipe> recipes = recipeRepository.findByMenuItemIdWithIngredient(menuItemId);
        
        log.info("[GET_RECIPE] Found {} ingredients for menuItemId: {}", recipes.size(), menuItemId);
        
        return recipes.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    private RecipeDto toDto(Recipe recipe) {
        return RecipeDto.builder()
                .id(recipe.getId())
                .menuItemId(recipe.getMenuItemId())
                .ingredientId(recipe.getIngredient().getId())
                .ingredientName(recipe.getIngredient().getName())
                .ingredientUnit(recipe.getIngredient().getUnit())
                .quantity(recipe.getQuantity())
                .notes(recipe.getNotes())
                .createdAt(recipe.getCreatedAt())
                .updatedAt(recipe.getUpdatedAt())
                .build();
    }
}
