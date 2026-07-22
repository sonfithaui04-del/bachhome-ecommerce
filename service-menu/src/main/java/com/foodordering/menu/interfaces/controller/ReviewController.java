package com.foodordering.menu.interfaces.controller;

import com.foodordering.menu.application.dto.CreateReviewDto;
import com.foodordering.menu.application.dto.ReviewDto;
import com.foodordering.menu.application.usecase.CreateReviewUseCase;
import com.foodordering.menu.application.usecase.GetReviewsUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/menu/reviews")
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
    public ResponseEntity<List<ReviewDto>> getReviewsByMenuItem(@PathVariable("itemId") Long itemId) {
        List<ReviewDto> reviews = getReviewsUseCase.execute(itemId);
        return ResponseEntity.ok(reviews);
    }
}
