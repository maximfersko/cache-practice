package com.fersko.cacheredis.controller.v1;

import com.fersko.cacheredis.dto.InventoryDto;
import com.fersko.cacheredis.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {
    
    private final InventoryService inventoryService;
    
    @PostMapping
    public ResponseEntity<InventoryDto> createInventory(@RequestBody InventoryDto inventoryDto) {
        InventoryDto createdInventory = inventoryService.createInventory(inventoryDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdInventory);
    }
    
    @GetMapping("/product/{productId}/warehouse/{warehouseId}")
    public ResponseEntity<InventoryDto> getInventory(
            @PathVariable UUID productId,
            @PathVariable Long warehouseId) {
        return inventoryService.getInventory(productId, warehouseId)
                .map(inventory -> ResponseEntity.ok(inventory))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<InventoryDto>> getInventoryByProductId(@PathVariable UUID productId) {
        List<InventoryDto> inventory = inventoryService.getInventoryByProductId(productId);
        return ResponseEntity.ok(inventory);
    }
    
    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<InventoryDto>> getInventoryByWarehouseId(@PathVariable Long warehouseId) {
        List<InventoryDto> inventory = inventoryService.getInventoryByWarehouseId(warehouseId);
        return ResponseEntity.ok(inventory);
    }
    
    @GetMapping("/available")
    public ResponseEntity<List<InventoryDto>> getAvailableInventory() {
        List<InventoryDto> inventory = inventoryService.getAvailableInventory();
        return ResponseEntity.ok(inventory);
    }
    
    @GetMapping("/available/product/{productId}")
    public ResponseEntity<List<InventoryDto>> getAvailableInventoryByProductId(@PathVariable UUID productId) {
        List<InventoryDto> inventory = inventoryService.getAvailableInventoryByProductId(productId);
        return ResponseEntity.ok(inventory);
    }
    
    @PutMapping("/product/{productId}/warehouse/{warehouseId}")
    public ResponseEntity<InventoryDto> updateInventory(
            @PathVariable UUID productId,
            @PathVariable Long warehouseId,
            @RequestBody InventoryDto inventoryDto) {
        try {
            InventoryDto updatedInventory = inventoryService.updateInventory(productId, warehouseId, inventoryDto);
            return ResponseEntity.ok(updatedInventory);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/product/{productId}/warehouse/{warehouseId}")
    public ResponseEntity<Void> deleteInventory(
            @PathVariable UUID productId,
            @PathVariable Long warehouseId) {
        try {
            inventoryService.deleteInventory(productId, warehouseId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/exists")
    public ResponseEntity<Boolean> existsByProductIdAndWarehouseId(
            @RequestParam UUID productId,
            @RequestParam Long warehouseId) {
        boolean exists = inventoryService.existsByProductIdAndWarehouseId(productId, warehouseId);
        return ResponseEntity.ok(exists);
    }
}
