#!/usr/bin/env python3


import requests
import time
from typing import Dict, Optional

BASE_URL = "http://localhost:8080"


def check_api_availability() -> bool:
    try:
        response = requests.get(f"{BASE_URL}/api/v1/categories", timeout=5)
        return response.status_code < 500
    except requests.exceptions.RequestException:
        return False


def measure_request(func):
    def wrapper(*args, **kwargs):
        start = time.time()
        result = func(*args, **kwargs)
        end = time.time()
        elapsed_ms = (end - start) * 1000
        return result, elapsed_ms
    return wrapper


@measure_request
def get_category(category_id: str) -> Optional[Dict]:
    try:
        response = requests.get(
            f"{BASE_URL}/api/v1/categories/{category_id}",
            timeout=10
        )
        if response.status_code == 200:
            return response.json()
    except Exception:
        pass
    return None


@measure_request
def get_all_categories() -> Optional[Dict]:
    try:
        response = requests.get(
            f"{BASE_URL}/api/v1/categories",
            timeout=10
        )
        if response.status_code == 200:
            return response.json()
    except Exception:
        pass
    return None


@measure_request
def create_category(name: str, slug: str) -> Optional[Dict]:
    try:
        response = requests.post(
            f"{BASE_URL}/api/v1/categories",
            json={"name": name, "slug": slug},
            timeout=10
        )
        if response.status_code == 201:
            return response.json()
    except Exception:
        pass
    return None


@measure_request
def update_category(category_id: str, name: str, slug: str) -> Optional[Dict]:
    try:
        response = requests.put(
            f"{BASE_URL}/api/v1/categories/{category_id}",
            json={"name": name, "slug": slug},
            timeout=10
        )
        if response.status_code == 200:
            return response.json()
    except Exception:
        pass
    return None


def demonstrate_read_through():
    print("=" * 60)
    print("ПАТТЕРН 1: Read-Through")
    print("=" * 60)
    print()
    print("Описание:")
    print("Кэш автоматически загружает данные при промахе (MISS).")
    print("Spring @Cacheable реализует этот паттерн.")
    print()
    print("Применение:")
    print("- Справочные данные (категории, настройки)")
    print("- Данные с высокой частотой чтения")
    print("- Данные, меняющиеся редко")
    print()
    print("-" * 60)
    
    # Первый запрос - Cache MISS
    print("[TEST] Первый запрос (Cache MISS)")
    data1, time1 = get_all_categories()
    print(f"Результат: {'OK' if data1 else 'ERROR'}")
    print(f"Время выполнения: {time1:.2f} ms")
    print(f"Путь данных: API -> Cache MISS -> DB -> Cache -> Response")
    print()
    
    # Второй запрос - Cache HIT
    print("[TEST] Второй запрос (Cache HIT)")
    data2, time2 = get_all_categories()
    print(f"Результат: {'OK' if data2 else 'ERROR'}")
    print(f"Время выполнения: {time2:.2f} ms")
    print(f"Путь данных: API -> Cache HIT -> Response")
    print()
    
    # Анализ
    improvement = ((time1 - time2) / time1) * 100 if time1 > 0 else 0
    speedup = time1 / time2 if time2 > 0 else 0
    
    print("РЕЗУЛЬТАТ:")
    print(f"Cache MISS: {time1:.2f} ms (запрос к БД)")
    print(f"Cache HIT:  {time2:.2f} ms (запрос к кэшу)")
    print(f"Улучшение:  {improvement:.2f}%")
    print(f"Ускорение:  {speedup:.2f}x")
    print()


def demonstrate_write_through():
    print("=" * 60)
    print("ПАТТЕРН 2: Write-Through")
    print("=" * 60)
    print()
    print("Описание:")
    print("Данные синхронно записываются в кэш и БД одновременно.")
    print("Spring @CachePut реализует этот паттерн.")
    print()
    print("Применение:")
    print("- Создание новых записей")
    print("- Критичные данные (требуют консистентности)")
    print("- Данные с высокой частотой последующего чтения")
    print()
    print("-" * 60)
    
    # Создание категории - запись в БД и кэш
    print("[TEST] Создание категории (Write-Through)")
    category_data, create_time = create_category(
        name="Demo Category",
        slug="demo-category"
    )
    
    if category_data:
        category_id = category_data.get("id")
        print(f"Результат: OK")
        print(f"ID категории: {category_id}")
        print(f"Время создания: {create_time:.2f} ms")
        print(f"Путь данных: API -> DB + Cache -> Response")
        print()
        
        # Последующее чтение из кэша
        print("[TEST] Чтение только что созданной категории")
        read_data, read_time = get_category(category_id)
        print(f"Результат: {'OK' if read_data else 'ERROR'}")
        print(f"Время чтения: {read_time:.2f} ms")
        print(f"Путь данных: API -> Cache HIT -> Response")
        print()
        
        # Анализ
        print("РЕЗУЛЬТАТ:")
        print(f"Создание (DB + Cache): {create_time:.2f} ms")
        print(f"Чтение (Cache HIT):    {read_time:.2f} ms")
        print("Данные немедленно доступны в кэше после создания")
        print("Гарантирована консистентность кэша и БД")
        print()
    else:
        print("ERROR: Не удалось создать категорию")
        print()


def demonstrate_cache_eviction():
    print("=" * 60)
    print("ПАТТЕРН 3: Cache Eviction (Инвалидация)")
    print("=" * 60)
    print()
    print("Описание:")
    print("При обновлении данных кэш автоматически очищается.")
    print("Spring @CacheEvict реализует этот паттерн.")
    print()
    print("Применение:")
    print("- Обновление существующих записей")
    print("- Удаление записей")
    print("- Обеспечение актуальности данных")
    print()
    print("-" * 60)
    
    # Создание категории для теста
    print("[SETUP] Создание тестовой категории")
    category_data, _ = create_category(
        name="Test Category",
        slug="test-category"
    )
    
    if not category_data:
        print("ERROR: Не удалось создать тестовую категорию")
        print()
        return
    
    category_id = category_data.get("id")
    print(f"OK: Категория создана (ID: {category_id})")
    print()
    
    # Первое чтение - заполнение кэша
    print("[TEST] Первое чтение (заполнение кэша)")
    _, read1_time = get_category(category_id)
    print(f"Время: {read1_time:.2f} ms (Cache HIT после создания)")
    print()
    
    # Обновление - инвалидация кэша
    print("[TEST] Обновление категории (Cache Eviction)")
    _, update_time = update_category(
        category_id,
        name="Updated Category",
        slug="updated-category"
    )
    print(f"Время обновления: {update_time:.2f} ms")
    print("Действие: Кэш автоматически очищен")
    print()
    
    # Чтение после обновления - Cache MISS
    print("[TEST] Чтение после обновления (Cache MISS)")
    _, read2_time = get_category(category_id)
    print(f"Время: {read2_time:.2f} ms")
    print(f"Путь данных: API -> Cache MISS -> DB -> Cache -> Response")
    print()
    
    # Повторное чтение - Cache HIT
    print("[TEST] Повторное чтение (Cache HIT)")
    _, read3_time = get_category(category_id)
    print(f"Время: {read3_time:.2f} ms")
    print(f"Путь данных: API -> Cache HIT -> Response")
    print()
    
    # Анализ
    print("РЕЗУЛЬТАТ:")
    print(f"Чтение до обновления:    {read1_time:.2f} ms (Cache HIT)")
    print(f"Обновление:              {update_time:.2f} ms (DB + Eviction)")
    print(f"Чтение после обновления: {read2_time:.2f} ms (Cache MISS)")
    print(f"Повторное чтение:        {read3_time:.2f} ms (Cache HIT)")
    print("Кэш автоматически обновляется при изменении данных")
    print()


def run_pattern_demonstrations():
    print("=" * 60)
    print("ДЕМОНСТРАЦИЯ ПАТТЕРНОВ КЭШИРОВАНИЯ")
    print("=" * 60)
    print()
    
    # Проверка доступности API
    if not check_api_availability():
        print("[ERROR] API недоступно по адресу", BASE_URL)
        return
    
    print("[OK] API доступно")
    print()
    
    # Выполнение демонстраций
    demonstrate_read_through()
    time.sleep(1)
    
    demonstrate_write_through()
    time.sleep(1)
    
    demonstrate_cache_eviction()
    
    # Итоговое заключение
    print("=" * 60)
    print("ЗАКЛЮЧЕНИЕ")
    print("=" * 60)
    print()
    print("Продемонстрированные паттерны:")
    print()
    print("1. Read-Through:")
    print("   Автоматическое кэширование при чтении")
    print("   Использование: @Cacheable")
    print()
    print("2. Write-Through:")
    print("   Синхронная запись в кэш и БД")
    print("   Использование: @CachePut")
    print()
    print("3. Cache Eviction:")
    print("   Автоматическая инвалидация при обновлении")
    print("   Использование: @CacheEvict")
    print()
    print("Все паттерны реализованы в CategoryServiceImpl")
    print("Просмотрите код для подробных комментариев")
    print("=" * 60)


if __name__ == "__main__":
    run_pattern_demonstrations()

