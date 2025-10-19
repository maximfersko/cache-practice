package com.fersko.cacheredis.repository;

import com.fersko.cacheredis.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    
    List<Review> findByProductId(UUID productId);
    
    List<Review> findByRating(Short rating);
    
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId")
    List<Review> findByProductIdWithProduct(@Param("productId") UUID productId);
    
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.rating >= :minRating")
    List<Review> findByProductIdAndMinRating(@Param("productId") UUID productId, @Param("minRating") Short minRating);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Optional<Double> findAverageRatingByProductId(@Param("productId") UUID productId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId")
    Long countByProductId(@Param("productId") UUID productId);
}
