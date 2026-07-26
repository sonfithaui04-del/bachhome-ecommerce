package com.bachhome.inventory.domain.repository;

import com.bachhome.inventory.domain.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho InventoryItem
 */
@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    
    /**
     * Tìm mặt hàng trong kho theo tên
     */
    Optional<InventoryItem> findByName(String name);

    /**
     * Tìm mặt hàng trong kho gắn với một sản phẩm đang bán
     */
    Optional<InventoryItem> findByProductId(Long productId);
    
    /**
     * Lấy tất cả mặt hàng trong kho đang active
     */
    List<InventoryItem> findByActiveTrue();
    
    /**
     * Lấy các mặt hàng trong kho sắp hết (quantity <= minQuantity)
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.active = true AND i.quantity <= i.minQuantity AND i.minQuantity IS NOT NULL")
    List<InventoryItem> findLowStockInventoryItems();
}
