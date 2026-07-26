package com.bachhome.inventory.interfaces.controller;

import com.bachhome.inventory.application.dto.CreateInventoryItemDto;
import com.bachhome.inventory.application.dto.InventoryItemDto;
import com.bachhome.inventory.application.usecase.CreateInventoryItemUseCase;
import com.bachhome.inventory.application.usecase.GetAllInventoryItemsUseCase;
import com.bachhome.inventory.application.usecase.UpdateInventoryItemUseCase;
import com.bachhome.inventory.domain.repository.InventoryItemRepository;
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
 * Controller cho InventoryItem Management
 */
@RestController
@RequestMapping("/inventory-items")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "InventoryItems", description = "API quản lý mặt hàng trong kho")
public class InventoryItemController {
    
    private final CreateInventoryItemUseCase createInventoryItemUseCase;
    private final GetAllInventoryItemsUseCase getAllInventoryItemsUseCase;
    private final UpdateInventoryItemUseCase updateInventoryItemUseCase;
    private final InventoryItemRepository inventoryItemRepository;
    
    @GetMapping
    @Operation(summary = "Lấy tất cả mặt hàng trong kho")
    public ResponseEntity<List<InventoryItemDto>> getAllInventoryItems(
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        log.info("[INVENTORY-ITEM-CONTROLLER] Get all inventoryItems, activeOnly: {}", activeOnly);
        List<InventoryItemDto> inventoryItems = getAllInventoryItemsUseCase.execute(activeOnly);
        return ResponseEntity.ok(inventoryItems);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Lấy mặt hàng trong kho theo ID")
    public ResponseEntity<InventoryItemDto> getInventoryItemById(@PathVariable Long id) {
        log.info("[INVENTORY-ITEM-CONTROLLER] Get inventoryItem by ID: {}", id);
        return inventoryItemRepository.findById(id)
                .map(inventoryItem -> {
                    InventoryItemDto dto = InventoryItemDto.builder()
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
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-product/{productId}")
    @Operation(summary = "Lấy mặt hàng trong kho gắn với một sản phẩm đang bán")
    public ResponseEntity<InventoryItemDto> getInventoryItemByProductId(@PathVariable Long productId) {
        log.info("[INVENTORY-ITEM-CONTROLLER] Get inventoryItem by productId: {}", productId);
        return inventoryItemRepository.findByProductId(productId)
                .map(inventoryItem -> {
                    InventoryItemDto dto = InventoryItemDto.builder()
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
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Tạo mặt hàng trong kho mới")
    public ResponseEntity<InventoryItemDto> createInventoryItem(@Valid @RequestBody CreateInventoryItemDto dto) {
        log.info("[INVENTORY-ITEM-CONTROLLER] Create inventoryItem: {}", dto.getName());
        InventoryItemDto created = createInventoryItemUseCase.execute(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật mặt hàng trong kho")
    public ResponseEntity<InventoryItemDto> updateInventoryItem(
            @PathVariable Long id,
            @Valid @RequestBody CreateInventoryItemDto dto) {
        log.info("[INVENTORY-ITEM-CONTROLLER] Update inventoryItem ID: {}", id);
        InventoryItemDto updated = updateInventoryItemUseCase.execute(id, dto);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mặt hàng trong kho (soft delete)")
    public ResponseEntity<Void> deleteInventoryItem(@PathVariable Long id) {
        log.info("[INVENTORY-ITEM-CONTROLLER] Delete inventoryItem ID: {}", id);
        return inventoryItemRepository.findById(id)
                .map(inventoryItem -> {
                    inventoryItem.setActive(false);
                    inventoryItemRepository.save(inventoryItem);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health Check")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Inventory Service is running");
    }
}
