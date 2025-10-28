package com.fersko.cacheredis.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;


@Configuration
@EnableCaching
public class CacheConfig {
    
    /**
     * L1 Cache Manager - Caffeine (локальный кэш в памяти JVM).
     * 
     * Eviction Policy:
     * - expireAfterWrite(5 минут): TTL с момента записи
     * - expireAfterAccess(2 минуты): Продление жизни при чтении
     * - maximumSize(1000): LRU вытеснение при переполнении
     * 
     * @return CacheManager для локального кэша
     */
    @Bean
    @Primary
    public CacheManager localCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(5))
                .expireAfterAccess(Duration.ofMinutes(2))
                .recordStats());
        
        return cacheManager;
    }
    
    /**
     * L2 Cache Manager - Redis.
     * @param connectionFactory фабрика подключений к Redis
     * @return CacheManager для распределенного кэша
     */
    @Bean("redisCacheManager")
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        // Создание дифференцированных конфигураций для разных типов данных
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Конфигурация для справочных данных (categories)
        // TTL 1 час - данные меняются редко
        cacheConfigurations.put("categories", 
                createCacheConfig(Duration.ofHours(1)));
        
        // Конфигурация для продуктов
        // TTL 30 минут - данные обновляются чаще
        cacheConfigurations.put("products", 
                createCacheConfig(Duration.ofMinutes(30)));
        
        // Конфигурация для цен
        // TTL 15 минут - цены могут меняться часто
        cacheConfigurations.put("prices", 
                createCacheConfig(Duration.ofMinutes(15)));
        
        // Конфигурация для результатов поиска
        // TTL 5 минут - балансируем актуальность и производительность
        cacheConfigurations.put("searchResults", 
                createCacheConfig(Duration.ofMinutes(5)));
        
        RedisCacheConfiguration defaultConfig = createCacheConfig(Duration.ofMinutes(10));
        
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
    
    /**
     * Вспомогательный метод для создания конфигурации Redis кэша с заданным TTL.
     * @param ttl время жизни записи в кэше
     * @return конфигурация Redis кэша
     */
    private RedisCacheConfiguration createCacheConfig(Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
                // Установка TTL для записей
                .entryTtl(ttl)
                
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                
                // Сериализация значений в JSON формат
                // GenericJackson2JsonRedisSerializer автоматически сохраняет информацию о типе
                // Это позволяет десериализовать объекты обратно в правильные Java типы
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                        
                // Отключение кэширования null значений
                // null в кэше может маскировать проблемы с данными
                .disableCachingNullValues();
    }
}
