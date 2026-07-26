package com.bachhome.product.domain.repository;

import com.bachhome.product.domain.model.Product;
import java.util.List;
import java.util.Optional;

/**
 * Product Repository Interface (Domain Layer)
 */
public interface ProductRepository {
    Optional<Product> findById(Long id);
    List<Product> findAll();
    List<Product> findAllAvailable();
    List<Product> findByCategoryId(Long categoryId);
    Product save(Product product);
    void deleteById(Long id);
}
