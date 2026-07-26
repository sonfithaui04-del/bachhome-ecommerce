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
 * Use Case: Tạo mặt hàng trong kho mới
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreateInventoryItemUseCase {
    
    private final InventoryItemRepository inventoryItemRepository;
    
    @Transactional
    public InventoryItemDto execute(CreateInventoryItemDto dto) {
        log.info("[CREATE_INVENTORY_ITEM] Creating inventoryItem: {}", dto.getName());
        
        // Kiểm tra tên đã tồn tại chưa
        if (inventoryItemRepository.findByName(dto.getName()).isPresent()) {
            throw new RuntimeException("Mặt hàng trong kho với tên này đã tồn tại");
        }
        
        InventoryItem inventoryItem = InventoryItem.builder()
                .name(dto.getName())
                .productId(dto.getProductId())
                .unit(dto.getUnit())
                .quantity(dto.getQuantity())
                .minQuantity(dto.getMinQuantity())
                .costPerUnit(dto.getCostPerUnit())
                .expiryDate(dto.getExpiryDate())
                .description(dto.getDescription())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();
        
        InventoryItem saved = inventoryItemRepository.save(inventoryItem);
        log.info("[CREATE_INVENTORY_ITEM] InventoryItem created with ID: {}", saved.getId());
        
        return toDto(saved);
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
