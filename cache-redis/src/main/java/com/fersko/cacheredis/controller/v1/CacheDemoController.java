package com.fersko.cacheredis.controller.v1;

import com.fersko.cacheredis.dto.CategoryDto;
import com.fersko.cacheredis.service.CacheDemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cache-demo")
@RequiredArgsConstructor
public class CacheDemoController {
    
    private final CacheDemoService cacheDemoService;
    
    @GetMapping("/cache-aside/{id}")
    public ResponseEntity<CategoryDto> demonstrateCacheAside(@PathVariable UUID id) {
        CategoryDto category = cacheDemoService.getCategoryWithCacheAside(id);
        return ResponseEntity.ok(category);
    }
    
    @PostMapping("/write-through")
    public ResponseEntity<CategoryDto> demonstrateWriteThrough(@RequestBody CategoryDto categoryDto) {
        CategoryDto createdCategory = cacheDemoService.createCategoryWithWriteThrough(categoryDto);
        return ResponseEntity.ok(createdCategory);
    }
    
    @PutMapping("/write-behind/{id}")
    public ResponseEntity<CategoryDto> demonstrateWriteBehind(
            @PathVariable UUID id, 
            @RequestBody CategoryDto categoryDto) {
        CategoryDto updatedCategory = cacheDemoService.updateCategoryWithWriteBehind(id, categoryDto);
        return ResponseEntity.ok(updatedCategory);
    }
    
    @PostMapping("/warm-up")
    public ResponseEntity<String> warmUpCache() {
        cacheDemoService.warmUpCache();
        return ResponseEntity.ok("Cache warmed up successfully");
    }
    
    @GetMapping("/hit-miss/{id}")
    public ResponseEntity<String> demonstrateCacheHitMiss(@PathVariable UUID id) {
        String result = cacheDemoService.demonstrateCacheHitMiss(id);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/slow-operation/{input}")
    public ResponseEntity<String> demonstrateSlowOperation(@PathVariable String input) {
        String result = cacheDemoService.slowOperation(input);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/ttl/{key}")
    public ResponseEntity<String> demonstrateTTL(@PathVariable String key) {
        String result = cacheDemoService.getDataWithTTL(key);
        return ResponseEntity.ok(result);
    }
    
    @DeleteMapping("/evict")
    public ResponseEntity<String> demonstrateCacheEviction() {
        cacheDemoService.demonstrateCacheEviction();
        return ResponseEntity.ok("Cache evicted successfully");
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<String> getCacheStatistics() {
        String statistics = cacheDemoService.getCacheStatistics();
        return ResponseEntity.ok(statistics);
    }
}
