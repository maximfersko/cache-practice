package com.fersko.cacheredis.service;

import com.fersko.cacheredis.dto.InventoryDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface InventoryService {
    

    InventoryDto createInventory(InventoryDto inventoryDto);

    Optional<InventoryDto> getInventory(UUID productId, Long warehouseId);

    List<InventoryDto> getInventoryByProductId(UUID productId);

    List<InventoryDto> getInventoryByWarehouseId(Long warehouseId);

    List<InventoryDto> getAvailableInventory();

    List<InventoryDto> getAvailableInventoryByProductId(UUID productId);

    InventoryDto updateInventory(UUID productId, Long warehouseId, InventoryDto inventoryDto);

    void deleteInventory(UUID productId, Long warehouseId);

    boolean existsByProductIdAndWarehouseId(UUID productId, Long warehouseId);
}
