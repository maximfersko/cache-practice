package com.fersko.cacheredis.service.impl;

import com.fersko.cacheredis.dto.CategoryDto;
import com.fersko.cacheredis.entity.Category;
import com.fersko.cacheredis.mappers.CategoryMapper;
import com.fersko.cacheredis.repository.CategoryRepository;
import com.fersko.cacheredis.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с категориями товаров.
 * 
 * Демонстрирует паттерны кэширования:
 * - Read-Through: Автоматическое кэширование при чтении (@Cacheable)
 * - Write-Through: Синхронная запись в кэш при создании (@CachePut)
 * - Explicit Invalidation: Явное удаление из кэша при обновлении (@CacheEvict)
 * 
 * Используемый кэш:
 * - localCacheManager (Caffeine): L1 локальный кэш
 * - TTL: 5 минут с момента записи
 * - Eviction: LRU при достижении 1000 записей
 * 
 * Обоснование выбора локального кэша:
 * - Категории - справочные данные, меняются редко
 * - Читаются очень часто (почти каждый запрос товаров)
 * - Небольшой объем данных (обычно 10-100 категорий)
 * - Не требуется строгая синхронизация между инстансами
 * - Допустима eventual consistency (данные обновятся через TTL)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    
    /**
     * Создание новой категории.
     * 
     * Паттерн: Write-Through
     * Аннотация: @CachePut
     * 
     * Логика работы:
     * 1. Метод выполняется всегда (в отличие от @Cacheable)
     * 2. Категория сохраняется в БД
     * 3. Результат автоматически кладется в кэш по ключу result.id
     * 4. Последующие запросы этой категории будут из кэша (~1ms)
     * 
     * Обоснование паттерна:
     * - Write-Through обеспечивает немедленную доступность созданной категории
     * - Нет необходимости в дополнительном запросе для кэширования
     * - Гарантирует консистентность кэша и БД
     * 
     * Ключ кэша: categories::{uuid}
     * Пример: categories::123e4567-e89b-12d3-a456-426614174000
     * 
     * @param categoryDto данные новой категории
     * @return созданная категория с ID
     */
    @Override
    @CachePut(value = "categories", key = "#result.id", cacheManager = "localCacheManager")
    public CategoryDto createCategory(CategoryDto categoryDto) {
        // Конвертация DTO в entity
        Category category = categoryMapper.toEntity(categoryDto);
        
        // Сохранение в БД (~50ms)
        Category savedCategory = categoryRepository.save(category);
        
        // Конвертация entity обратно в DTO
        // Spring автоматически положит результат в кэш после return
        return categoryMapper.toDto(savedCategory);
    }
    
    /**
     * Получение категории по ID.
     * 
     * Паттерн: Без кэширования (для демонстрации)
     * 
     * Этот метод намеренно НЕ кэшируется для демонстрации разницы
     * в производительности между кэшированными и некэшированными запросами.
     * 
     * Производительность:
     * - Каждый вызов идет в БД (~50ms)
     * - Нагрузка на БД высокая при частых запросах
     * - Подходит только для редко используемых операций
     * 
     * Для production:
     * Рекомендуется добавить @Cacheable для часто запрашиваемых категорий
     * 
     * @param id UUID категории
     * @return Optional с категорией или empty
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryDto> getCategoryById(UUID id) {
        return categoryRepository.findById(id)
                .map(categoryMapper::toDto);
    }
    
    /**
     * Получение категории по slug (человекопонятному идентификатору).
     * 
     * Паттерн: Read-Through (Кэширование при чтении)
     * Аннотация: @Cacheable
     * 
     * Логика работы:
     * 1. Spring проверяет кэш по ключу "slug:{slug}"
     * 2. Если данные есть (Cache HIT) -> возврат из кэша (~1ms)
     * 3. Если данных нет (Cache MISS):
     *    - Выполняется метод (запрос в БД ~50ms)
     *    - Результат автоматически кэшируется
     *    - Следующий запрос будет из кэша
     * 
     * Обоснование использования:
     * - Slug часто используется в URL (example.com/category/electronics)
     * - Один и тот же slug запрашивается многократно
     * - Категории по slug меняются очень редко
     * - Идеальный кандидат для кэширования
     * 
     * Стратегия ключа:
     * - Префикс "slug:" для избежания коллизий с ID
     * - Ключ включает сам slug для уникальности
     * 
     * Ключ кэша: categories::slug:{slug}
     * Пример: categories::slug:electronics
     * 
     * @param slug человекопонятный идентификатор категории
     * @return Optional с категорией или empty
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "'slug:' + #slug", cacheManager = "localCacheManager")
    public Optional<CategoryDto> getCategoryBySlug(String slug) {
        // Этот код выполняется только при Cache MISS
        return categoryRepository.findBySlug(slug)
                .map(categoryMapper::toDto);
    }
    
    /**
     * Получение всех категорий.
     * 
     * Паттерн: Read-Through (Кэширование списков)
     * Аннотация: @Cacheable
     * 
     * Логика работы:
     * 1. Проверка кэша по ключу "all"
     * 2. Cache HIT -> возврат списка из кэша (~1ms)
     * 3. Cache MISS -> загрузка из БД + кэширование (~50ms)
     * 
     * Обоснование использования:
     * - Список всех категорий запрашивается очень часто
     * - Используется для построения меню навигации
     * - Отображается на главной странице
     * - Данные редко меняются (новые категории добавляются нечасто)
     * 
     * Стратегия инвалидации:
     * - TTL 5 минут (автоматическое обновление)
     * - При создании/обновлении категории кэш сбрасывается вручную (@CacheEvict)
     * 
     * Особенности:
     * - Кэшируется весь список целиком
     * - Один ключ для всех категорий (не по каждой категории)
     * - Эффективно для небольшого количества категорий (10-100 шт)
     * 
     * Альтернатива для больших объемов:
     * - Пагинация с кэшированием по номеру страницы
     * - Кэширование по группам/типам категорий
     * 
     * Ключ кэша: categories::all
     * 
     * @return список всех категорий
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "'all'", cacheManager = "localCacheManager")
    public List<CategoryDto> getAllCategories() {
        // Этот код выполняется только при Cache MISS
        // Загрузка всех категорий из БД и маппинг в DTO
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toDto)
                .toList();
    }
    
    /**
     * Обновление существующей категории.
     * 
     * Паттерн: Write-Through с Explicit Invalidation
     * Аннотация: @CacheEvict
     * 
     * Логика работы:
     * 1. Категория обновляется в БД
     * 2. Spring автоматически удаляет устаревшие данные из кэша
     * 3. Следующий запрос категории пойдет в БД (Cache MISS)
     * 4. Свежие данные будут закэшированы при следующем чтении
     * 
     * Стратегия инвалидации:
     * - Удаляем конкретную категорию по ID (key = "#id")
     * - Удаляем кэш всех категорий (allEntries = true)
     * - Это гарантирует консистентность данных
     * 
     * Обоснование:
     * - Обновление категории - редкая операция
     * - Критично обеспечить актуальность данных
     * - Небольшая задержка при следующем чтении приемлема
     * 
     * Альтернативный подход (@CachePut):
     * - Можно использовать @CachePut для немедленного обновления кэша
     * - Но тогда нужно очищать связанные кэши (список всех, поиск)
     * - @CacheEvict проще и надежнее для обеспечения консистентности
     * 
     * @param id UUID категории для обновления
     * @param categoryDto новые данные категории
     * @return обновленная категория
     */
    @Override
    @CacheEvict(value = "categories", allEntries = true, cacheManager = "localCacheManager")
    public CategoryDto updateCategory(UUID id, CategoryDto categoryDto) {
        // Поиск существующей категории
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        
        // Обновление полей
        existingCategory.setName(categoryDto.name());
        existingCategory.setSlug(categoryDto.slug());
        
        // Сохранение в БД
        Category updatedCategory = categoryRepository.save(existingCategory);
        
        // После выполнения метода Spring очистит кэш категорий
        return categoryMapper.toDto(updatedCategory);
    }
    
    /**
     * Удаление категории.
     * 
     * Паттерн: Explicit Invalidation
     * Аннотация: @CacheEvict
     * 
     * Логика работы:
     * 1. Проверка существования категории
     * 2. Удаление из БД
     * 3. Автоматическое удаление всех связанных записей из кэша
     * 
     * Стратегия инвалидации:
     * - allEntries = true: полная очистка кэша категорий
     * - Гарантирует отсутствие "мертвых" ссылок в кэше
     * - Простая и надежная стратегия для редких операций
     * 
     * Обоснование:
     * - Удаление категории - очень редкая операция
     * - Критична консистентность (не должно быть ссылок на удаленную категорию)
     * - Полная очистка кэша приемлема (быстро восстановится при следующих запросах)
     * 
     * @param id UUID категории для удаления
     * @throws RuntimeException если категория не найдена
     */
    @Override
    @CacheEvict(value = "categories", allEntries = true, cacheManager = "localCacheManager")
    public void deleteCategory(UUID id) {
        // Проверка существования перед удалением
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found with id: " + id);
        }
        
        // Удаление из БД
        categoryRepository.deleteById(id);
        
        // Spring автоматически очистит весь кэш категорий после выполнения
    }
    
    /**
     * Поиск категорий по поисковому запросу.
     * 
     * Паттерн: Read-Through (Кэширование результатов поиска)
     * Аннотация: @Cacheable
     * 
     * Логика работы:
     * 1. Проверка кэша по ключу "search:{term}"
     * 2. Cache HIT -> возврат результатов из кэша (~1ms)
     * 3. Cache MISS -> выполнение поиска в БД + кэширование (~50-100ms)
     * 
     * Обоснование использования:
     * - Пользователи часто повторяют одинаковые поисковые запросы
     * - Поиск по БД может быть медленным (LIKE запросы, полнотекстовый поиск)
     * - Кэширование результатов значительно улучшает UX
     * 
     * Особенности:
     * - Каждый уникальный поисковый запрос кэшируется отдельно
     * - Префикс "search:" предотвращает коллизии с другими ключами
     * - TTL 5 минут достаточен для актуальности результатов
     * 
     * Ограничения:
     * - Кэшируются только точные совпадения запроса
     * - "электроника" и "Электроника" - разные ключи
     * - Для production рекомендуется нормализация запроса (toLowerCase)
     * 
     * Оптимизация:
     * - Можно ограничить кэширование только популярных запросов
     * - Использовать отдельный TTL для поисковых запросов (короче)
     * - Добавить максимальный размер для предотвращения переполнения
     * 
     * Ключ кэша: categories::search:{searchTerm}
     * Пример: categories::search:electronics
     * 
     * @param searchTerm поисковый запрос
     * @return список найденных категорий
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "'search:' + #searchTerm", cacheManager = "localCacheManager")
    public List<CategoryDto> searchCategories(String searchTerm) {
        // Выполнение поиска в БД только при Cache MISS
        // Поиск может включать LIKE запросы, поэтому может быть медленным
        return categoryRepository.findBySearchTerm(searchTerm)
                .stream()
                .map(categoryMapper::toDto)
                .toList();
    }
    
    /**
     * Проверка существования категории по slug.
     * 
     * Паттерн: Без кэширования
     * 
     * Этот метод НЕ кэшируется намеренно, так как:
     * - Используется для валидации при создании/обновлении
     * - Требует актуальных данных в реальном времени
     * - Вызывается редко (только при изменении данных)
     * - Результат boolean - минимальный overhead запроса к БД
     * 
     * Для проверки существования критична актуальность данных,
     * поэтому кэширование здесь может привести к race conditions.
     * 
     * @param slug slug для проверки
     * @return true если категория с таким slug существует
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsBySlug(String slug) {
        return categoryRepository.existsBySlug(slug);
    }
}
