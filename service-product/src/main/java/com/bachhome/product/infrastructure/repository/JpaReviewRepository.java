package com.bachhome.product.infrastructure.repository;

import com.bachhome.product.domain.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaReviewRepository extends JpaRepository<Review, Long> {
    
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId ORDER BY r.createdAt DESC")
    List<Review> findByProductId(@Param("productId") Long productId);

    List<Review> findByUserId(Long userId);
}
