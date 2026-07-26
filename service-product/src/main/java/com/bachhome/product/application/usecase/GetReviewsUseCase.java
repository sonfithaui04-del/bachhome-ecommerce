package com.bachhome.product.application.usecase;

import com.bachhome.product.application.dto.ReviewDto;
import com.bachhome.product.domain.model.Review;
import com.bachhome.product.domain.repository.ReviewRepository;
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
    public List<ReviewDto> execute(Long productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        return reviews.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private ReviewDto mapToDto(Review review) {
        return ReviewDto.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .userId(review.getUserId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
