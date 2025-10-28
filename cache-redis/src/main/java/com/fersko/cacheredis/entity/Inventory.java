package com.fersko.cacheredis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {
    
    @EmbeddedId
    private InventoryId id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, insertable = false, updatable = false)
    private Product product;
    
    @Column(name = "warehouse_id", nullable = false, insertable = false, updatable = false)
    private Long warehouseId;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
    
    @Embeddable
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryId {
        @Column(name = "product_id")
        private UUID productId;
        
        @Column(name = "warehouse_id")
        private Long warehouseId;
    }
}
