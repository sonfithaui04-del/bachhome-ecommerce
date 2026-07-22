package com.foodordering.menu.application.usecase;

import com.foodordering.menu.application.dto.ReviewDto;
import com.foodordering.menu.domain.model.Review;
import com.foodordering.menu.domain.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetReviewsUseCase {

    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public List<ReviewDto> execute(Long menuItemId) {
        List<Review> reviews = reviewRepository.findByMenuItemId(menuItemId);
        return reviews.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private ReviewDto mapToDto(Review review) {
        return ReviewDto.builder()
                .id(review.getId())
                .menuItemId(review.getMenuItem().getId())
                .userId(review.getUserId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
