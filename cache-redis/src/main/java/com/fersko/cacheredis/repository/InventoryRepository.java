package com.fersko.cacheredis.repository;

import com.fersko.cacheredis.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Inventory.InventoryId> {
    
    List<Inventory> findByProductId(UUID productId);
    
    List<Inventory> findByWarehouseId(Long warehouseId);
    
    Optional<Inventory> findByProductIdAndWarehouseId(UUID productId, Long warehouseId);
    
    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId")
    List<Inventory> findByProductIdWithProduct(@Param("productId") UUID productId);
    
    @Query("SELECT i FROM Inventory i WHERE i.quantity > 0")
    List<Inventory> findAvailableInventory();
    
    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId AND i.quantity > 0")
    List<Inventory> findAvailableInventoryByProductId(@Param("productId") UUID productId);
    
    boolean existsByProductIdAndWarehouseId(UUID productId, Long warehouseId);
}
