package com.foodordering.inventory.domain.repository;

import com.foodordering.inventory.domain.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho Recipe
 */
@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    
    /**
     * Lấy tất cả công thức của một món ăn
     */
    List<Recipe> findByMenuItemId(Long menuItemId);
    
    /**
     * Lấy tất cả công thức của một món ăn (eager loading ingredient)
     */
    @Query("SELECT r FROM Recipe r JOIN FETCH r.ingredient WHERE r.menuItemId = :menuItemId")
    List<Recipe> findByMenuItemIdWithIngredient(Long menuItemId);
    
    /**
     * Xóa tất cả công thức của một món ăn
     */
    void deleteByMenuItemId(Long menuItemId);
    
    /**
     * Kiểm tra món ăn đã có công thức chưa
     */
    boolean existsByMenuItemId(Long menuItemId);
}
