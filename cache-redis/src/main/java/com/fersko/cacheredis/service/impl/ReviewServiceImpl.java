package com.fersko.cacheredis.service.impl;

import com.fersko.cacheredis.dto.ReviewDto;
import com.fersko.cacheredis.entity.Product;
import com.fersko.cacheredis.entity.Review;
import com.fersko.cacheredis.mappers.ReviewMapper;
import com.fersko.cacheredis.repository.ProductRepository;
import com.fersko.cacheredis.repository.ReviewRepository;
import com.fersko.cacheredis.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final ReviewMapper reviewMapper;
    
    @Override
    public ReviewDto createReview(ReviewDto reviewDto) {
        Review review = reviewMapper.toEntity(reviewDto);
        
        Product product = productRepository.findById(reviewDto.productId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + reviewDto.productId()));
        review.setProduct(product);
        
        Review savedReview = reviewRepository.save(review);
        return reviewMapper.toDto(savedReview);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ReviewDto> getReviewById(UUID id) {
        return reviewRepository.findById(id)
                .map(reviewMapper::toDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsByProductId(UUID productId) {
        return reviewRepository.findByProductId(productId)
                .stream()
                .map(reviewMapper::toDto)
                .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsByRating(Short rating) {
        return reviewRepository.findByRating(rating)
                .stream()
                .map(reviewMapper::toDto)
                .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsByProductIdAndMinRating(UUID productId, Short minRating) {
        return reviewRepository.findByProductIdAndMinRating(productId, minRating)
                .stream()
                .map(reviewMapper::toDto)
                .toList();
    }
    
    @Override
    public ReviewDto updateReview(UUID id, ReviewDto reviewDto) {
        Review existingReview = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));
        
        existingReview.setRating(reviewDto.rating());
        existingReview.setText(reviewDto.text());
        
        Review updatedReview = reviewRepository.save(existingReview);
        return reviewMapper.toDto(updatedReview);
    }
    
    @Override
    public void deleteReview(UUID id) {
        if (!reviewRepository.existsById(id)) {
            throw new RuntimeException("Review not found with id: " + id);
        }
        reviewRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Double> getAverageRatingByProductId(UUID productId) {
        return reviewRepository.findAverageRatingByProductId(productId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long countReviewsByProductId(UUID productId) {
        return reviewRepository.countByProductId(productId);
    }
}
