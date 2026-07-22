package com.foodordering.menu.infrastructure.repository;

import com.foodordering.menu.domain.model.Review;
import com.foodordering.menu.domain.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepository {

    private final JpaReviewRepository jpaRepository;

    @Override
    public Optional<Review> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Review> findByMenuItemId(Long menuItemId) {
        return jpaRepository.findByMenuItemId(menuItemId);
    }

    @Override
    public List<Review> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public Review save(Review review) {
        return jpaRepository.save(review);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
