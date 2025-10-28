package com.fersko.cacheredis.service;

import com.fersko.cacheredis.dto.CategoryDto;
import com.fersko.cacheredis.dto.ProductDto;

import java.util.List;
import java.util.UUID;

public interface CacheDemoService {
    
    CategoryDto getCategoryWithCacheAside(UUID id);
    
    CategoryDto createCategoryWithWriteThrough(CategoryDto categoryDto);
    
    CategoryDto updateCategoryWithWriteBehind(UUID id, CategoryDto categoryDto);
    
    void warmUpCache();
    
    String demonstrateCacheHitMiss(UUID categoryId);
    
    String slowOperation(String input);
    
    String getDataWithTTL(String key);
    
    void demonstrateCacheEviction();
    
    String getCacheStatistics();
}
