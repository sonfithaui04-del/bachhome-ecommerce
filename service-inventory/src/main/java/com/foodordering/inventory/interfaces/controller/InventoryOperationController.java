package com.foodordering.inventory.interfaces.controller;

import com.foodordering.inventory.application.dto.DeductInventoryDto;
import com.foodordering.inventory.application.usecase.DeductInventoryUseCase;
import com.foodordering.inventory.application.usecase.RestoreInventoryUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller để quản lý inventory operations (deduct/restore)
 */
@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inventory Operations", description = "API trừ/hoàn nguyên liệu")
public class InventoryOperationController {
    
    private final DeductInventoryUseCase deductInventoryUseCase;
    private final RestoreInventoryUseCase restoreInventoryUseCase;
    
    @PostMapping("/deduct")
    @Operation(summary = "Trừ nguyên liệu khi confirm đơn hàng")
    public ResponseEntity<String> deductInventory(@RequestBody DeductInventoryDto dto) {
        log.info("[INVENTORY-OPERATION] Deduct request for order: {}", dto.getOrderId());
        try {
            deductInventoryUseCase.execute(dto);
            return ResponseEntity.ok("Inventory deducted successfully");
        } catch (Exception e) {
            log.error("[INVENTORY-OPERATION] Deduct failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/restore")
    @Operation(summary = "Hoàn lại nguyên liệu khi cancel đơn hàng")
    public ResponseEntity<String> restoreInventory(@RequestBody DeductInventoryDto dto) {
        log.info("[INVENTORY-OPERATION] Restore request for order: {}", dto.getOrderId());
        try {
            restoreInventoryUseCase.execute(dto);
            return ResponseEntity.ok("Inventory restored successfully");
        } catch (Exception e) {
            log.error("[INVENTORY-OPERATION] Restore failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
