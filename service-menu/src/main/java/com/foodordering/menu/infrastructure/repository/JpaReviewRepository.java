package com.foodordering.menu.infrastructure.repository;

import com.foodordering.menu.domain.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaReviewRepository extends JpaRepository<Review, Long> {
    
    @Query("SELECT r FROM Review r WHERE r.menuItem.id = :menuItemId ORDER BY r.createdAt DESC")
    List<Review> findByMenuItemId(@Param("menuItemId") Long menuItemId);

    List<Review> findByUserId(Long userId);
}
