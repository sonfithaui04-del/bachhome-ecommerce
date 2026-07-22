package com.foodordering.inventory.domain.repository;

import com.foodordering.inventory.domain.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho Ingredient
 */
@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    
    /**
     * Tìm nguyên liệu theo tên
     */
    Optional<Ingredient> findByName(String name);
    
    /**
     * Lấy tất cả nguyên liệu đang active
     */
    List<Ingredient> findByActiveTrue();
    
    /**
     * Lấy các nguyên liệu sắp hết (quantity <= minQuantity)
     */
    @Query("SELECT i FROM Ingredient i WHERE i.active = true AND i.quantity <= i.minQuantity AND i.minQuantity IS NOT NULL")
    List<Ingredient> findLowStockIngredients();
}
