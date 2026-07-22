package com.bachhome.menu.infrastructure.repository;

import com.bachhome.menu.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * JPA Repository cho Category
 */
@Repository
public interface JpaCategoryRepository extends JpaRepository<Category, Long> {
    
    @Query("SELECT c FROM Category c WHERE c.active = true ORDER BY c.displayOrder")
    List<Category> findAllActive();
    
    boolean existsByName(String name);
}
