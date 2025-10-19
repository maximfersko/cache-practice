package com.fersko.cacheredis.repository;

import com.fersko.cacheredis.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    
    Optional<Category> findBySlug(String slug);
    
    List<Category> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT c FROM Category c WHERE c.name LIKE %:searchTerm% OR c.slug LIKE %:searchTerm%")
    List<Category> findBySearchTerm(@Param("searchTerm") String searchTerm);
    
    boolean existsBySlug(String slug);
}
