package com.foodordering.inventory.interfaces.controller;

import com.foodordering.inventory.application.dto.CreateIngredientDto;
import com.foodordering.inventory.application.dto.IngredientDto;
import com.foodordering.inventory.application.usecase.CreateIngredientUseCase;
import com.foodordering.inventory.application.usecase.GetAllIngredientsUseCase;
import com.foodordering.inventory.application.usecase.UpdateIngredientUseCase;
import com.foodordering.inventory.domain.repository.IngredientRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller cho Ingredient Management
 */
@RestController
@RequestMapping("/ingredients")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Ingredients", description = "API quản lý nguyên liệu")
public class IngredientController {
    
    private final CreateIngredientUseCase createIngredientUseCase;
    private final GetAllIngredientsUseCase getAllIngredientsUseCase;
    private final UpdateIngredientUseCase updateIngredientUseCase;
    private final IngredientRepository ingredientRepository;
    
    @GetMapping
    @Operation(summary = "Lấy tất cả nguyên liệu")
    public ResponseEntity<List<IngredientDto>> getAllIngredients(
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        log.info("[INGREDIENT-CONTROLLER] Get all ingredients, activeOnly: {}", activeOnly);
        List<IngredientDto> ingredients = getAllIngredientsUseCase.execute(activeOnly);
        return ResponseEntity.ok(ingredients);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Lấy nguyên liệu theo ID")
    public ResponseEntity<IngredientDto> getIngredientById(@PathVariable Long id) {
        log.info("[INGREDIENT-CONTROLLER] Get ingredient by ID: {}", id);
        return ingredientRepository.findById(id)
                .map(ingredient -> {
                    IngredientDto dto = IngredientDto.builder()
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
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @Operation(summary = "Tạo nguyên liệu mới")
    public ResponseEntity<IngredientDto> createIngredient(@Valid @RequestBody CreateIngredientDto dto) {
        log.info("[INGREDIENT-CONTROLLER] Create ingredient: {}", dto.getName());
        IngredientDto created = createIngredientUseCase.execute(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật nguyên liệu")
    public ResponseEntity<IngredientDto> updateIngredient(
            @PathVariable Long id,
            @Valid @RequestBody CreateIngredientDto dto) {
        log.info("[INGREDIENT-CONTROLLER] Update ingredient ID: {}", id);
        IngredientDto updated = updateIngredientUseCase.execute(id, dto);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa nguyên liệu (soft delete)")
    public ResponseEntity<Void> deleteIngredient(@PathVariable Long id) {
        log.info("[INGREDIENT-CONTROLLER] Delete ingredient ID: {}", id);
        return ingredientRepository.findById(id)
                .map(ingredient -> {
                    ingredient.setActive(false);
                    ingredientRepository.save(ingredient);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health Check")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Inventory Service is running");
    }
}
