package com.bachhome.menu.infrastructure.repository;

import com.bachhome.menu.domain.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * JPA Repository cho MenuItem
 */
@Repository
public interface JpaMenuItemRepository extends JpaRepository<MenuItem, Long> {
    
    @Query("SELECT m FROM MenuItem m WHERE m.available = true ORDER BY m.displayOrder")
    List<MenuItem> findAllAvailable();
    
    List<MenuItem> findByCategoryId(Long categoryId);
}
