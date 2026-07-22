package com.foodordering.inventory.interfaces.controller;

import com.foodordering.inventory.application.dto.RecipeDto;
import com.foodordering.inventory.application.dto.SetRecipeDto;
import com.foodordering.inventory.application.usecase.GetRecipeByMenuItemIdUseCase;
import com.foodordering.inventory.application.usecase.SetRecipeUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller cho Recipe Management
 */
@RestController
@RequestMapping("/recipes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Recipes", description = "API quản lý công thức món ăn")
public class RecipeController {
    
    private final SetRecipeUseCase setRecipeUseCase;
    private final GetRecipeByMenuItemIdUseCase getRecipeByMenuItemIdUseCase;
    
    @GetMapping("/menu-item/{menuItemId}")
    @Operation(summary = "Lấy công thức của món ăn")
    public ResponseEntity<List<RecipeDto>> getRecipeByMenuItemId(@PathVariable Long menuItemId) {
        log.info("[RECIPE-CONTROLLER] Get recipe for menuItemId: {}", menuItemId);
        List<RecipeDto> recipes = getRecipeByMenuItemIdUseCase.execute(menuItemId);
        return ResponseEntity.ok(recipes);
    }
    
    @PostMapping
    @Operation(summary = "Set công thức cho món ăn")
    public ResponseEntity<List<RecipeDto>> setRecipe(@Valid @RequestBody SetRecipeDto dto) {
        log.info("[RECIPE-CONTROLLER] Set recipe for menuItemId: {}", dto.getMenuItemId());
        List<RecipeDto> recipes = setRecipeUseCase.execute(dto);
        return ResponseEntity.ok(recipes);
    }
}
