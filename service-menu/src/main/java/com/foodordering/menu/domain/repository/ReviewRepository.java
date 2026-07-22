package com.foodordering.menu.domain.repository;

import com.foodordering.menu.domain.model.Review;
import java.util.List;
import java.util.Optional;

/**
 * Review Repository Interface (Domain Layer)
 */
public interface ReviewRepository {
    Optional<Review> findById(Long id);
    List<Review> findByMenuItemId(Long menuItemId);
    List<Review> findByUserId(Long userId);
    Review save(Review review);
    void deleteById(Long id);
}
