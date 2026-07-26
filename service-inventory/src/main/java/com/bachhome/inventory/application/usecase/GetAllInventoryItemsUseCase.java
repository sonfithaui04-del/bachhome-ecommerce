package com.bachhome.inventory.application.usecase;

import com.bachhome.inventory.application.dto.InventoryItemDto;
import com.bachhome.inventory.domain.model.InventoryItem;
import com.bachhome.inventory.domain.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Use Case: Lấy tất cả mặt hàng trong kho
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetAllInventoryItemsUseCase {
    
    private final InventoryItemRepository inventoryItemRepository;
    
    @Transactional(readOnly = true)
    public List<InventoryItemDto> execute(boolean activeOnly) {
        log.info("[GET_ALL_INVENTORY_ITEMS] Fetching inventoryItems, activeOnly: {}", activeOnly);
        
        List<InventoryItem> inventoryItems = activeOnly 
                ? inventoryItemRepository.findByActiveTrue()
                : inventoryItemRepository.findAll();
        
        log.info("[GET_ALL_INVENTORY_ITEMS] Found {} inventoryItems", inventoryItems.size());
        
        return inventoryItems.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    private InventoryItemDto toDto(InventoryItem inventoryItem) {
        return InventoryItemDto.builder()
                .id(inventoryItem.getId())
                .name(inventoryItem.getName())
                .productId(inventoryItem.getProductId())
                .unit(inventoryItem.getUnit())
                .quantity(inventoryItem.getQuantity())
                .minQuantity(inventoryItem.getMinQuantity())
                .costPerUnit(inventoryItem.getCostPerUnit())
                .expiryDate(inventoryItem.getExpiryDate())
                .description(inventoryItem.getDescription())
                .active(inventoryItem.getActive())
                .isLowStock(inventoryItem.isLowStock())
                .createdAt(inventoryItem.getCreatedAt())
                .updatedAt(inventoryItem.getUpdatedAt())
                .build();
    }
}
