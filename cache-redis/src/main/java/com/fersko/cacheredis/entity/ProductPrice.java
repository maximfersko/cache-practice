package com.fersko.cacheredis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "product_price")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPrice {
    
    @EmbeddedId
    private ProductPriceId id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, insertable = false, updatable = false)
    private Product product;
    
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
    
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
    
    @Embeddable
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductPriceId {
        @Column(name = "product_id")
        private UUID productId;
        
        @Column(name = "currency", length = 3)
        private String currency;
    }
}
