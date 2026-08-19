package com.bachhome.product.application.usecase;

import com.bachhome.product.application.dto.ReviewDto;
import com.bachhome.product.domain.model.Review;
import com.bachhome.product.domain.repository.ReviewRepository;
import com.bachhome.product.infrastructure.client.AuthServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetReviewsUseCase {

    private final ReviewRepository reviewRepository;
    private final AuthServiceClient authServiceClient;

    @Transactional
    public List<ReviewDto> execute(Long productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);

        // Đánh giá cũ chưa có tên thì hỏi Auth Service một lần rồi lưu lại
        for (Review review : reviews) {
            if (review.getUserName() == null || review.getUserName().isBlank()) {
                String name = authServiceClient.getDisplayName(review.getUserId());
                if (name != null && !name.isBlank()) {
                    review.setUserName(name);
                    reviewRepository.save(review);
                }
            }
        }

        return reviews.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private ReviewDto mapToDto(Review review) {
        return ReviewDto.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .userId(review.getUserId())
                .userName(review.getUserName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
