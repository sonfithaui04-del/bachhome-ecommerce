package com.bachhome.product.interfaces.controller;

import com.bachhome.product.application.dto.CreateReviewDto;
import com.bachhome.product.application.dto.ReviewDto;
import com.bachhome.product.application.usecase.CreateReviewUseCase;
import com.bachhome.product.application.usecase.GetReviewsUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final CreateReviewUseCase createReviewUseCase;
    private final GetReviewsUseCase getReviewsUseCase;

    @PostMapping
    public ResponseEntity<ReviewDto> addReview(@Valid @RequestBody CreateReviewDto dto,
                                               @RequestHeader(value = "X-User-Id", required = false) String userIdStr) {
        
        if (userIdStr == null || userIdStr.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = Long.parseLong(userIdStr);
        ReviewDto review = createReviewUseCase.execute(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByProduct(@PathVariable("itemId") Long itemId) {
        List<ReviewDto> reviews = getReviewsUseCase.execute(itemId);
        return ResponseEntity.ok(reviews);
    }
}
