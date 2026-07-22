package com.foodordering.menu.application.usecase;

import com.foodordering.menu.application.dto.CreateMenuItemDto;
import com.foodordering.menu.application.dto.MenuItemDto;
import com.foodordering.menu.domain.model.Category;
import com.foodordering.menu.domain.model.MenuItem;
import com.foodordering.menu.domain.repository.CategoryRepository;
import com.foodordering.menu.domain.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Tạo menu item mới
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreateMenuItemUseCase {

    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public MenuItemDto execute(CreateMenuItemDto request) {
        log.info("[CREATE_MENU_ITEM] Creating menu item: {}", request.getName());
        
        // Kiểm tra category có tồn tại không
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> {
                    log.error("[CREATE_MENU_ITEM] Category not found: {}", request.getCategoryId());
                    return new RuntimeException("Category không tồn tại");
                });
        
        // Tạo menu item entity
        MenuItem menuItem = MenuItem.builder()
                .category(category)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .available(true)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .build();
        
        // Lưu vào database
        MenuItem savedItem = menuItemRepository.save(menuItem);
        log.info("[CREATE_MENU_ITEM] Menu item created with ID: {}", savedItem.getId());
        
        return toDto(savedItem);
    }

    private MenuItemDto toDto(MenuItem item) {
        return MenuItemDto.builder()
                .id(item.getId())
                .categoryId(item.getCategory().getId())
                .categoryName(item.getCategory().getName())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .imageUrl(item.getImageUrl())
                .available(item.getAvailable())
                .displayOrder(item.getDisplayOrder())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
