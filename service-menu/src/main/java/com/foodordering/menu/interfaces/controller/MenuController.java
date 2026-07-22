package com.foodordering.menu.interfaces.controller;

import com.foodordering.menu.application.dto.CreateMenuItemDto;
import com.foodordering.menu.application.dto.MenuItemDto;
import com.foodordering.menu.application.usecase.CreateMenuItemUseCase;
import com.foodordering.menu.application.usecase.GetAllMenuItemsUseCase;
import com.foodordering.menu.domain.model.Category;
import com.foodordering.menu.domain.model.MenuItem;
import com.foodordering.menu.domain.repository.CategoryRepository;
import com.foodordering.menu.domain.repository.MenuItemRepository;
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
 * Menu Controller - REST API for Menu Items
 */
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Menu", description = "API quản lý món ăn")
public class MenuController {

    private final GetAllMenuItemsUseCase getAllMenuItemsUseCase;
    private final CreateMenuItemUseCase createMenuItemUseCase;
    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Lấy tất cả menu items
     * GET /menu
     */
    @GetMapping
    @Operation(summary = "Lấy danh sách món ăn", description = "Lấy tất cả món ăn hoặc chỉ món available")
    public ResponseEntity<List<MenuItemDto>> getAllMenuItems(
            @RequestParam(defaultValue = "true") boolean availableOnly) {
        log.info("[MENU-CONTROLLER] Get all menu items, availableOnly: {}", availableOnly);
        List<MenuItemDto> menuItems = getAllMenuItemsUseCase.execute(availableOnly);
        return ResponseEntity.ok(menuItems);
    }

    /**
     * Tạo menu item mới
     * POST /menu
     */
    @PostMapping
    @Operation(summary = "Tạo món ăn mới", description = "Thêm món ăn mới vào menu")
    public ResponseEntity<MenuItemDto> createMenuItem(@Valid @RequestBody CreateMenuItemDto request) {
        log.info("[MENU-CONTROLLER] Create menu item: {}", request.getName());
        MenuItemDto menuItem = createMenuItemUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(menuItem);
    }

    /**
     * Cập nhật menu item
     * PUT /menu/{id}
     */
    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật món ăn", description = "Cập nhật thông tin món ăn")
    public ResponseEntity<MenuItemDto> updateMenuItem(
            @PathVariable Long id,
            @Valid @RequestBody CreateMenuItemDto request) {
        log.info("[MENU-CONTROLLER] Update menu item: {}", id);
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));
        
        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setImageUrl(request.getImageUrl());
        
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            menuItem.setCategory(category);
        }
        
        if (request.getAvailable() != null) {
            menuItem.setAvailable(request.getAvailable());
        }
        
        if (request.getDisplayOrder() != null) {
            menuItem.setDisplayOrder(request.getDisplayOrder());
        }
        
        MenuItem updated = menuItemRepository.save(menuItem);
        return ResponseEntity.ok(convertToDto(updated));
    }

    /**
     * Xóa menu item
     * DELETE /menu/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa món ăn", description = "Xóa món ăn khỏi menu")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
        log.info("[MENU-CONTROLLER] Delete menu item: {}", id);
        menuItemRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    @Operation(summary = "Health Check")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Menu Controller is running");
    }

    private MenuItemDto convertToDto(MenuItem menuItem) {
        MenuItemDto dto = new MenuItemDto();
        dto.setId(menuItem.getId());
        dto.setName(menuItem.getName());
        dto.setDescription(menuItem.getDescription());
        dto.setPrice(menuItem.getPrice());
        dto.setImageUrl(menuItem.getImageUrl());
        dto.setDisplayOrder(menuItem.getDisplayOrder());
        if (menuItem.getCategory() != null) {
            dto.setCategoryId(menuItem.getCategory().getId());
            dto.setCategoryName(menuItem.getCategory().getName());
        }
        dto.setAvailable(menuItem.getAvailable() != null && menuItem.getAvailable());
        dto.setCreatedAt(menuItem.getCreatedAt());
        dto.setUpdatedAt(menuItem.getUpdatedAt());
        return dto;
    }
}
