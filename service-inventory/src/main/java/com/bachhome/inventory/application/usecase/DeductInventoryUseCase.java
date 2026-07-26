package com.bachhome.inventory.application.usecase;

import com.bachhome.inventory.application.dto.DeductInventoryDto;
import com.bachhome.inventory.domain.model.InventoryItem;
import com.bachhome.inventory.domain.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Use Case: Trừ mặt hàng trong kho khi confirm order
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeductInventoryUseCase {
    
    private final InventoryItemRepository inventoryItemRepository;
    
    @Transactional
    public void execute(DeductInventoryDto dto) {
        log.info("[DEDUCT_INVENTORY] Processing order ID: {}", dto.getOrderId());
        
        List<String> insufficientItems = new ArrayList<>();
        
        // 1. Kiểm tra tất cả mặt hàng trong kho trước
        for (DeductInventoryDto.InventoryDeductionDto item : dto.getInventoryItems()) {
            InventoryItem inventoryItem = inventoryItemRepository.findById(item.getInventoryItemId())
                    .orElseThrow(() -> new RuntimeException("InventoryItem not found: " + item.getInventoryItemId()));
            
            if (inventoryItem.getQuantity().compareTo(item.getQuantity()) < 0) {
                insufficientItems.add(String.format("%s (Cần: %s %s, Còn: %s %s)", 
                    inventoryItem.getName(),
                    item.getQuantity(),
                    inventoryItem.getUnit(),
                    inventoryItem.getQuantity(),
                    inventoryItem.getUnit()
                ));
            }
        }
        
        // 2. Nếu có mặt hàng trong kho không đủ -> throw exception
        if (!insufficientItems.isEmpty()) {
            String errorMsg = "Mặt hàng trong kho không đủ: " + String.join(", ", insufficientItems);
            log.error("[DEDUCT_INVENTORY] {}", errorMsg);
            throw new RuntimeException(errorMsg);
        }
        
        // 3. Trừ mặt hàng trong kho
        for (DeductInventoryDto.InventoryDeductionDto item : dto.getInventoryItems()) {
            InventoryItem inventoryItem = inventoryItemRepository.findById(item.getInventoryItemId()).get();
            inventoryItem.deductQuantity(item.getQuantity());
            inventoryItemRepository.save(inventoryItem);
            
            log.info("[DEDUCT_INVENTORY] Deducted {} {} of {}", 
                item.getQuantity(), inventoryItem.getUnit(), inventoryItem.getName());
        }
        
        log.info("[DEDUCT_INVENTORY] Successfully deducted inventory for order {}", dto.getOrderId());
    }
}
