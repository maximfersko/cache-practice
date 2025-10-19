package com.fersko.cacheredis.controller.v1;

import com.fersko.cacheredis.dto.ReviewDto;
import com.fersko.cacheredis.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {
    
    private final ReviewService reviewService;
    
    @PostMapping
    public ResponseEntity<ReviewDto> createReview(@RequestBody ReviewDto reviewDto) {
        ReviewDto createdReview = reviewService.createReview(reviewDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ReviewDto> getReviewById(@PathVariable UUID id) {
        return reviewService.getReviewById(id)
                .map(review -> ResponseEntity.ok(review))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByProductId(@PathVariable UUID productId) {
        List<ReviewDto> reviews = reviewService.getReviewsByProductId(productId);
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/rating/{rating}")
    public ResponseEntity<List<ReviewDto>> getReviewsByRating(@PathVariable Short rating) {
        List<ReviewDto> reviews = reviewService.getReviewsByRating(rating);
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/product/{productId}/min-rating/{minRating}")
    public ResponseEntity<List<ReviewDto>> getReviewsByProductIdAndMinRating(
            @PathVariable UUID productId,
            @PathVariable Short minRating) {
        List<ReviewDto> reviews = reviewService.getReviewsByProductIdAndMinRating(productId, minRating);
        return ResponseEntity.ok(reviews);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ReviewDto> updateReview(@PathVariable UUID id, @RequestBody ReviewDto reviewDto) {
        try {
            ReviewDto updatedReview = reviewService.updateReview(id, reviewDto);
            return ResponseEntity.ok(updatedReview);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID id) {
        try {
            reviewService.deleteReview(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/product/{productId}/average-rating")
    public ResponseEntity<Double> getAverageRatingByProductId(@PathVariable UUID productId) {
        return reviewService.getAverageRatingByProductId(productId)
                .map(rating -> ResponseEntity.ok(rating))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/product/{productId}/count")
    public ResponseEntity<Long> countReviewsByProductId(@PathVariable UUID productId) {
        Long count = reviewService.countReviewsByProductId(productId);
        return ResponseEntity.ok(count);
    }
}
