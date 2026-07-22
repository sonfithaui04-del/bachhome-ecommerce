package com.foodordering.menu.application.usecase;

import com.foodordering.menu.application.dto.CreateReviewDto;
import com.foodordering.menu.application.dto.ReviewDto;
import com.foodordering.menu.domain.model.MenuItem;
import com.foodordering.menu.domain.model.Review;
import com.foodordering.menu.domain.repository.MenuItemRepository;
import com.foodordering.menu.domain.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateReviewUseCase {

    private final ReviewRepository reviewRepository;
    private final MenuItemRepository menuItemRepository;

    @Transactional
    public ReviewDto execute(CreateReviewDto dto, Long userId) {
        // 1. Validate MenuItem
        MenuItem menuItem = menuItemRepository.findById(dto.getMenuItemId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy món ăn với ID: " + dto.getMenuItemId()));

        // 2. Create Review
        Review review = Review.builder()
                .menuItem(menuItem)
                .userId(userId)
                .rating(dto.getRating())
                .comment(dto.getComment())
                .build();

        review = reviewRepository.save(review);

        // 3. Update MenuItem Rating
        updateMenuItemRating(menuItem);

        // 4. Return DTO
        return ReviewDto.builder()
                .id(review.getId())
                .menuItemId(menuItem.getId())
                .userId(review.getUserId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    private void updateMenuItemRating(MenuItem menuItem) {
        List<Review> reviews = reviewRepository.findByMenuItemId(menuItem.getId());
        if (reviews.isEmpty()) {
            menuItem.setAverageRating(0.0);
            menuItem.setTotalReviews(0);
        } else {
            double sum = 0;
            for (Review r : reviews) {
                sum += r.getRating();
            }
            menuItem.setAverageRating(sum / reviews.size());
            menuItem.setTotalReviews(reviews.size());
        }
        menuItemRepository.save(menuItem);
    }
}
