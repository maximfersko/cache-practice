package com.fersko.cacheredis.repository;

import com.fersko.cacheredis.entity.ProductPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductPriceRepository extends JpaRepository<ProductPrice, ProductPrice.ProductPriceId> {
    
    List<ProductPrice> findByProductId(UUID productId);
    
    Optional<ProductPrice> findByProductIdAndCurrency(UUID productId, String currency);
    
    @Query("SELECT pp FROM ProductPrice pp WHERE pp.product.id = :productId")
    List<ProductPrice> findByProductIdWithProduct(@Param("productId") UUID productId);
    
    @Query("SELECT pp FROM ProductPrice pp WHERE pp.currency = :currency")
    List<ProductPrice> findByCurrency(@Param("currency") String currency);
    
    boolean existsByProductIdAndCurrency(UUID productId, String currency);
}
