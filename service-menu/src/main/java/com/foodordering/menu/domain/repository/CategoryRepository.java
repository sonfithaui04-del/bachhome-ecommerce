package com.foodordering.menu.domain.repository;

import com.foodordering.menu.domain.model.Category;
import java.util.List;
import java.util.Optional;

/**
 * Category Repository Interface (Domain Layer)
 */
public interface CategoryRepository {
    Optional<Category> findById(Long id);
    List<Category> findAll();
    List<Category> findAllActive();
    Category save(Category category);
    void deleteById(Long id);
    boolean existsByName(String name);
}
