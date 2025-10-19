package com.fersko.cacheredis.service;

import com.fersko.cacheredis.dto.ReviewDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface ReviewService {

    ReviewDto createReview(ReviewDto reviewDto);

    Optional<ReviewDto> getReviewById(UUID id);

    List<ReviewDto> getReviewsByProductId(UUID productId);

    List<ReviewDto> getReviewsByRating(Short rating);

    List<ReviewDto> getReviewsByProductIdAndMinRating(UUID productId, Short minRating);

    ReviewDto updateReview(UUID id, ReviewDto reviewDto);

    void deleteReview(UUID id);

    Optional<Double> getAverageRatingByProductId(UUID productId);

    Long countReviewsByProductId(UUID productId);
}
