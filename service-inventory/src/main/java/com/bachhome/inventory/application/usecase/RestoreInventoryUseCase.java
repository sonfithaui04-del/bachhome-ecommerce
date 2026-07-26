package com.bachhome.inventory.application.usecase;

import com.bachhome.inventory.application.dto.DeductInventoryDto;
import com.bachhome.inventory.domain.model.InventoryItem;
import com.bachhome.inventory.domain.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Hoàn lại mặt hàng trong kho khi cancel order
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RestoreInventoryUseCase {
    
    private final InventoryItemRepository inventoryItemRepository;
    
    @Transactional
    public void execute(DeductInventoryDto dto) {
        log.info("[RESTORE_INVENTORY] Restoring inventory for order ID: {}", dto.getOrderId());
        
        for (DeductInventoryDto.InventoryDeductionDto item : dto.getInventoryItems()) {
            InventoryItem inventoryItem = inventoryItemRepository.findById(item.getInventoryItemId())
                    .orElseThrow(() -> new RuntimeException("InventoryItem not found: " + item.getInventoryItemId()));
            
            inventoryItem.addQuantity(item.getQuantity());
            inventoryItemRepository.save(inventoryItem);
            
            log.info("[RESTORE_INVENTORY] Restored {} {} of {}", 
                item.getQuantity(), inventoryItem.getUnit(), inventoryItem.getName());
        }
        
        log.info("[RESTORE_INVENTORY] Successfully restored inventory for order {}", dto.getOrderId());
    }
}
