package com.bachhome.product.infrastructure.repository;

import com.bachhome.product.domain.model.Product;
import com.bachhome.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

/**
 * Product Repository Implementation (Adapter Pattern)
 */
@Component
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final JpaProductRepository jpaRepository;

    @Override
    public Optional<Product> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Product> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<Product> findAllAvailable() {
        return jpaRepository.findAllAvailable();
    }

    @Override
    public List<Product> findByCategoryId(Long categoryId) {
        return jpaRepository.findByCategoryId(categoryId);
    }

    @Override
    public Product save(Product product) {
        return jpaRepository.save(product);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
