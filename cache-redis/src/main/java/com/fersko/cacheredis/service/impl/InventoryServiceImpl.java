package com.fersko.cacheredis.service.impl;

import com.fersko.cacheredis.dto.InventoryDto;
import com.fersko.cacheredis.entity.Inventory;
import com.fersko.cacheredis.entity.Product;
import com.fersko.cacheredis.mappers.InventoryMapper;
import com.fersko.cacheredis.repository.InventoryRepository;
import com.fersko.cacheredis.repository.ProductRepository;
import com.fersko.cacheredis.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl implements InventoryService {
    
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final InventoryMapper inventoryMapper;
    
    @Override
    public InventoryDto createInventory(InventoryDto inventoryDto) {
        Inventory inventory = inventoryMapper.toEntity(inventoryDto);
        
        Product product = productRepository.findById(inventoryDto.productId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + inventoryDto.productId()));
        inventory.setProduct(product);
        
        Inventory savedInventory = inventoryRepository.save(inventory);
        return inventoryMapper.toDto(savedInventory);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<InventoryDto> getInventory(UUID productId, Long warehouseId) {
        return inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .map(inventoryMapper::toDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InventoryDto> getInventoryByProductId(UUID productId) {
        return inventoryRepository.findByProductId(productId)
                .stream()
                .map(inventoryMapper::toDto)
                .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InventoryDto> getInventoryByWarehouseId(Long warehouseId) {
        return inventoryRepository.findByWarehouseId(warehouseId)
                .stream()
                .map(inventoryMapper::toDto)
                .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InventoryDto> getAvailableInventory() {
        return inventoryRepository.findAvailableInventory()
                .stream()
                .map(inventoryMapper::toDto)
                .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InventoryDto> getAvailableInventoryByProductId(UUID productId) {
        return inventoryRepository.findAvailableInventoryByProductId(productId)
                .stream()
                .map(inventoryMapper::toDto)
                .toList();
    }
    
    @Override
    public InventoryDto updateInventory(UUID productId, Long warehouseId, InventoryDto inventoryDto) {
        Inventory existingInventory = inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId + " and warehouse: " + warehouseId));
        
        existingInventory.setQuantity(inventoryDto.quantity());
        
        Inventory updatedInventory = inventoryRepository.save(existingInventory);
        return inventoryMapper.toDto(updatedInventory);
    }
    
    @Override
    public void deleteInventory(UUID productId, Long warehouseId) {
        Inventory inventory = inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId + " and warehouse: " + warehouseId));
        
        inventoryRepository.delete(inventory);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByProductIdAndWarehouseId(UUID productId, Long warehouseId) {
        return inventoryRepository.existsByProductIdAndWarehouseId(productId, warehouseId);
    }
}
