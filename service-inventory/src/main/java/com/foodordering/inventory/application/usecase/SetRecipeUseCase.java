package com.foodordering.inventory.application.usecase;

import com.foodordering.inventory.application.dto.RecipeDto;
import com.foodordering.inventory.application.dto.SetRecipeDto;
import com.foodordering.inventory.domain.model.Ingredient;
import com.foodordering.inventory.domain.model.Recipe;
import com.foodordering.inventory.domain.repository.IngredientRepository;
import com.foodordering.inventory.domain.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Use Case: Set công thức cho món ăn
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SetRecipeUseCase {
    
    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    
    @Transactional
    public List<RecipeDto> execute(SetRecipeDto dto) {
        log.info("[SET_RECIPE] Setting recipe for menuItemId: {}", dto.getMenuItemId());
        
        // Xóa công thức cũ (nếu có)
        recipeRepository.deleteByMenuItemId(dto.getMenuItemId());
        
        // Tạo công thức mới
        List<Recipe> recipes = dto.getIngredients().stream()
                .map(item -> {
                    Ingredient ingredient = ingredientRepository.findById(item.getIngredientId())
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy nguyên liệu với ID: " + item.getIngredientId()));
                    
                    return Recipe.builder()
                            .menuItemId(dto.getMenuItemId())
                            .ingredient(ingredient)
                            .quantity(item.getQuantity())
                            .notes(item.getNotes())
                            .build();
                })
                .collect(Collectors.toList());
        
        List<Recipe> saved = recipeRepository.saveAll(recipes);
        log.info("[SET_RECIPE] Created {} recipe items for menuItemId: {}", saved.size(), dto.getMenuItemId());
        
        return saved.stream()
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
