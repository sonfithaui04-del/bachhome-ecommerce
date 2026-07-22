package com.foodordering.menu.application.usecase;

import com.foodordering.menu.application.dto.MenuItemDto;
import com.foodordering.menu.domain.model.MenuItem;
import com.foodordering.menu.domain.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Use Case: Lấy tất cả menu items
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetAllMenuItemsUseCase {

    private final MenuItemRepository menuItemRepository;

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<MenuItemDto> execute(boolean availableOnly) {
        log.info("[GET_ALL_MENU_ITEMS] Fetching menu items, availableOnly: {}", availableOnly);
        
        List<MenuItem> menuItems = availableOnly 
            ? menuItemRepository.findAllAvailable()
            : menuItemRepository.findAll();
        
        log.info("[GET_ALL_MENU_ITEMS] Found {} menu items", menuItems.size());
        
        return menuItems.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private MenuItemDto toDto(MenuItem item) {
        MenuItemDto.MenuItemDtoBuilder builder = MenuItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .imageUrl(item.getImageUrl())
                .available(item.getAvailable())
                .displayOrder(item.getDisplayOrder())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt());

        if (item.getCategory() != null) {
            builder.categoryId(item.getCategory().getId())
                   .categoryName(item.getCategory().getName());
        }

        return builder.build();
    }
}
