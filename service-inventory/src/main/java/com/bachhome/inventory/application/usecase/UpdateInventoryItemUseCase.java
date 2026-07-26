package com.bachhome.inventory.application.usecase;

import com.bachhome.inventory.application.dto.CreateInventoryItemDto;
import com.bachhome.inventory.application.dto.InventoryItemDto;
import com.bachhome.inventory.domain.model.InventoryItem;
import com.bachhome.inventory.domain.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Cập nhật mặt hàng trong kho
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateInventoryItemUseCase {
    
    private final InventoryItemRepository inventoryItemRepository;
    
    @Transactional
    public InventoryItemDto execute(Long id, CreateInventoryItemDto dto) {
        log.info("[UPDATE_INVENTORY_ITEM] Updating inventoryItem ID: {}", id);
        
        InventoryItem inventoryItem = inventoryItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mặt hàng trong kho với ID: " + id));
        
        inventoryItem.setName(dto.getName());
        inventoryItem.setProductId(dto.getProductId());
        inventoryItem.setUnit(dto.getUnit());
        inventoryItem.setQuantity(dto.getQuantity());
        inventoryItem.setMinQuantity(dto.getMinQuantity());
        inventoryItem.setCostPerUnit(dto.getCostPerUnit());
        inventoryItem.setExpiryDate(dto.getExpiryDate());
        inventoryItem.setDescription(dto.getDescription());
        if (dto.getActive() != null) {
            inventoryItem.setActive(dto.getActive());
        }
        
        InventoryItem updated = inventoryItemRepository.save(inventoryItem);
        log.info("[UPDATE_INVENTORY_ITEM] InventoryItem updated: {}", updated.getId());
        
        return toDto(updated);
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
