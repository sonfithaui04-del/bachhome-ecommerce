package com.bachhome.product.infrastructure.repository;

import com.bachhome.product.domain.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * JPA Repository cho Product
 */
@Repository
public interface JpaProductRepository extends JpaRepository<Product, Long> {
    
    @Query("SELECT m FROM Product m WHERE m.available = true ORDER BY m.displayOrder")
    List<Product> findAllAvailable();
    
    List<Product> findByCategoryId(Long categoryId);
}
