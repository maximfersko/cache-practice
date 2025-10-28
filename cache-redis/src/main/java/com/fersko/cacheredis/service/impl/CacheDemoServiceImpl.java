package com.fersko.cacheredis.service.impl;

import com.fersko.cacheredis.dto.CategoryDto;
import com.fersko.cacheredis.entity.Category;
import com.fersko.cacheredis.mappers.CategoryMapper;
import com.fersko.cacheredis.repository.CategoryRepository;
import com.fersko.cacheredis.service.CacheDemoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheDemoServiceImpl implements CacheDemoService {
    
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    
    @Override
    @Cacheable(value = "categories", key = "#id", cacheManager = "localCacheManager")
    public CategoryDto getCategoryWithCacheAside(UUID id) {
        log.info("Cache-Aside: Загрузка категории {} из БД (Cache Miss)", id);
        
        simulateSlowOperation();
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        return categoryMapper.toDto(category);
    }
    
    @Override
    @CachePut(value = "categories", key = "#result.id", cacheManager = "localCacheManager")
    @Transactional
    public CategoryDto createCategoryWithWriteThrough(CategoryDto categoryDto) {
        log.info("Write-Through: Создание категории {} с записью в кэш и БД", categoryDto.name());
        
        Category category = categoryMapper.toEntity(categoryDto);
        Category savedCategory = categoryRepository.save(category);
        
        log.info("Write-Through: Категория {} сохранена в БД и кэше", savedCategory.getId());
        return categoryMapper.toDto(savedCategory);
    }
    
    @Override
    @CachePut(value = "categories", key = "#id", cacheManager = "localCacheManager")
    public CategoryDto updateCategoryWithWriteBehind(UUID id, CategoryDto categoryDto) {
        log.info("Write-Behind: Обновление категории {} в кэше", id);
        
        CategoryDto updatedDto = new CategoryDto(
                id,
                categoryDto.name(),
                categoryDto.slug(),
                categoryDto.createdAt()
        );
        
        updateCategoryInDatabaseAsync(id, categoryDto);
        
        log.info("Write-Behind: Категория {} обновлена в кэше, БД обновляется асинхронно", id);
        return updatedDto;
    }
    
    @Async
    public CompletableFuture<Void> updateCategoryInDatabaseAsync(UUID id, CategoryDto categoryDto) {
        log.info("Write-Behind: Асинхронное обновление категории {} в БД", id);
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        category.setName(categoryDto.name());
        category.setSlug(categoryDto.slug());
        categoryRepository.save(category);
        
        log.info("Write-Behind: Категория {} обновлена в БД", id);
        return CompletableFuture.completedFuture(null);
    }
    
    @Override
    @Cacheable(value = "categories", key = "'all'", cacheManager = "localCacheManager")
    public void warmUpCache() {
        log.info("Cache Warming: Предварительное заполнение кэша");
        
        List<Category> categories = categoryRepository.findAll();
        log.info("Cache Warming: Загружено {} категорий в кэш", categories.size());
    }
    
    @Override
    public String demonstrateCacheHitMiss(UUID categoryId) {
        StringBuilder result = new StringBuilder();
        
        long startTime = System.currentTimeMillis();
        getCategoryWithCacheAside(categoryId);
        long missTime = System.currentTimeMillis() - startTime;
        
        result.append("Cache Miss: ").append(missTime).append("ms\n");
        
        startTime = System.currentTimeMillis();
        getCategoryWithCacheAside(categoryId);
        long hitTime = System.currentTimeMillis() - startTime;
        
        result.append("Cache Hit: ").append(hitTime).append("ms\n");
        
        double improvement = ((double) (missTime - hitTime) / missTime) * 100;
        result.append("Улучшение: ").append(String.format("%.1f", improvement)).append("%\n");
        
        return result.toString();
    }
    
    @Override
    @Cacheable(value = "slow-operations", key = "#input", cacheManager = "localCacheManager")
    public String slowOperation(String input) {
        log.info("Медленная операция для входа: {}", input);
        
        simulateSlowOperation();
        
        String result = "Результат для: " + input + " в " + LocalDateTime.now();
        log.info("Медленная операция завершена: {}", result);
        
        return result;
    }
    
    @Override
    @Cacheable(value = "ttl-demo", key = "#key", cacheManager = "redisCacheManager")
    public String getDataWithTTL(String key) {
        log.info("TTL Demo: Загрузка данных для ключа {} с TTL", key);
        
        simulateSlowOperation();
        
        return "Данные для ключа: " + key + " (TTL: 10 минут)";
    }
    
    @Override
    @CacheEvict(value = "categories", allEntries = true, cacheManager = "localCacheManager")
    public void demonstrateCacheEviction() {
        log.info("Cache Eviction: Очистка всех записей из кэша categories");
    }
    
    @Override
    public String getCacheStatistics() {
        return """
                СТАТИСТИКА КЭШИРОВАНИЯ
                =========================
                Cache-Aside: Lazy Loading
                Write-Through: Синхронная запись
                Write-Behind: Асинхронная запись
                Cache Warming: Предварительное заполнение
                TTL: Время жизни записей
                Cache Eviction: Очистка кэша
                Multi-Level: L1 (Caffeine) + L2 (Redis)
                """;
    }
    
    private void simulateSlowOperation() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
