package com.bachhome.product.application.usecase;

import com.bachhome.product.application.dto.CreateReviewDto;
import com.bachhome.product.application.dto.ReviewDto;
import com.bachhome.product.domain.model.Product;
import com.bachhome.product.domain.model.Review;
import com.bachhome.product.domain.repository.ProductRepository;
import com.bachhome.product.domain.repository.ReviewRepository;
import com.bachhome.product.infrastructure.client.AuthServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateReviewUseCase {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final AuthServiceClient authServiceClient;

    @Transactional
    public ReviewDto execute(CreateReviewDto dto, Long userId) {
        // 1. Validate Product
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + dto.getProductId()));

        // 2. Create Review - lưu kèm tên người đánh giá lấy từ Auth Service
        Review review = Review.builder()
                .product(product)
                .userId(userId)
                .userName(authServiceClient.getDisplayName(userId))
                .rating(dto.getRating())
                .comment(dto.getComment())
                .build();

        review = reviewRepository.save(review);

        // 3. Update Product Rating
        updateProductRating(product);

        // 4. Return DTO
        return ReviewDto.builder()
                .id(review.getId())
                .productId(product.getId())
                .userId(review.getUserId())
                .userName(review.getUserName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    private void updateProductRating(Product product) {
        List<Review> reviews = reviewRepository.findByProductId(product.getId());
        if (reviews.isEmpty()) {
            product.setAverageRating(0.0);
            product.setTotalReviews(0);
        } else {
            double sum = 0;
            for (Review r : reviews) {
                sum += r.getRating();
            }
            product.setAverageRating(sum / reviews.size());
            product.setTotalReviews(reviews.size());
        }
        productRepository.save(product);
    }
}
