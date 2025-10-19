package com.fersko.cacheredis.repository;

import com.fersko.cacheredis.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    
    Optional<Product> findBySku(String sku);
    
    List<Product> findByCategoryId(UUID categoryId);
    
    List<Product> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:searchTerm% OR p.description LIKE %:searchTerm%")
    List<Product> findBySearchTerm(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT p FROM Product p JOIN p.category c WHERE c.id = :categoryId")
    List<Product> findByCategoryIdWithCategory(@Param("categoryId") UUID categoryId);
    
    boolean existsBySku(String sku);
}
