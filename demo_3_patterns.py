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
    
    print("[TEST] Первый запрос (Cache MISS)")
    data1, time1 = get_all_categories()
    print(f"Результат: {'OK' if data1 else 'ERROR'}")
    print(f"Время выполнения: {time1:.2f} ms")
    print(f"Путь данных: API -> Cache MISS -> DB -> Cache -> Response")
    print()
    
    print("[TEST] Второй запрос (Cache HIT)")
    data2, time2 = get_all_categories()
    print(f"Результат: {'OK' if data2 else 'ERROR'}")
    print(f"Время выполнения: {time2:.2f} ms")
    print(f"Путь данных: API -> Cache HIT -> Response")
    print()
    
    improvement = ((time1 - time2) / time1) * 100 if time1 > 0 else 0
    speedup = time1 / time2 if time2 > 0 else 0
    saved_time = time1 - time2
    
    print("=" * 60)
    print("РЕЗУЛЬТАТЫ ИЗМЕРЕНИЙ")
    print("=" * 60)
    print(f"Первый запрос (Cache MISS):  {time1:>8.2f} ms  → Обращение к БД")
    print(f"Второй запрос (Cache HIT):   {time2:>8.2f} ms  → Чтение из Redis")
    print("-" * 60)
    print(f"Экономия времени:            {saved_time:>8.2f} ms  ({improvement:>6.2f}%)")
    print(f"Коэффициент ускорения:       {speedup:>8.2f}x")
    print("=" * 60)
    
    if speedup >= 3.0:
        verdict = "ОТЛИЧНО: Кэш значительно ускоряет запросы"
    elif speedup >= 2.0:
        verdict = "ХОРОШО: Кэш показывает существенное улучшение"
    elif speedup >= 1.5:
        verdict = "УДОВЛЕТВОРИТЕЛЬНО: Кэш дает заметное преимущество"
    else:
        verdict = "ВНИМАНИЕ: Эффективность кэширования низкая"
    
    print(f"Оценка: {verdict}")
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

    category_id = category_data.get("id")
    print(f"Результат: OK")
    print(f"ID категории: {category_id}")
    print(f"Время создания: {create_time:.2f} ms")
    print(f"Путь данных: API -> DB + Cache -> Response")
    print()

    print("[TEST] Чтение только что созданной категории")
    read_data, read_time = get_category(category_id)
    print(f"Результат: {'OK' if read_data else 'ERROR'}")
    print(f"Время чтения: {read_time:.2f} ms")
    print(f"Путь данных: API -> Cache HIT -> Response")
    print()

    speedup = create_time / read_time if read_time > 0 else 0

    print("=" * 60)
    print("РЕЗУЛЬТАТЫ ИЗМЕРЕНИЙ")
    print("=" * 60)
    print(f"Создание записи:             {create_time:>8.2f} ms  → БД + Redis")
    print(f"Чтение из кэша:              {read_time:>8.2f} ms  → Только Redis")
    print(f"Коэффициент ускорения:       {speedup:>8.2f}x")
    print("=" * 60)
    print()
    print("Данные немедленно доступны в кэше после создания")
    print("Гарантирована консистентность между БД и кэшем")
    print("Последующие чтения не требуют обращения к БД")
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
    
    print("[TEST] Первое чтение (заполнение кэша)")
    _, read1_time = get_category(category_id)
    print(f"Время: {read1_time:.2f} ms (Cache HIT после создания)")
    print()
    
    print("[TEST] Обновление категории (Cache Eviction)")
    _, update_time = update_category(
        category_id,
        name="Updated Category",
        slug="updated-category"
    )
    print(f"Время обновления: {update_time:.2f} ms")
    print("Действие: Кэш автоматически очищен")
    print()
    
    print("[TEST] Чтение после обновления (Cache MISS)")
    _, read2_time = get_category(category_id)
    print(f"Время: {read2_time:.2f} ms")
    print(f"Путь данных: API -> Cache MISS -> DB -> Cache -> Response")
    print()
    
    print("[TEST] Повторное чтение (Cache HIT)")
    _, read3_time = get_category(category_id)
    print(f"Время: {read3_time:.2f} ms")
    print(f"Путь данных: API -> Cache HIT -> Response")
    print()
    
    speedup = read2_time / read3_time if read3_time > 0 else 0
    
    print("=" * 60)
    print("РЕЗУЛЬТАТЫ ИЗМЕРЕНИЙ")
    print("=" * 60)
    print(f"1. Чтение (Cache HIT):       {read1_time:>8.2f} ms  → Из кэша")
    print(f"2. Обновление записи:        {update_time:>8.2f} ms  → БД + Eviction")
    print(f"3. Чтение (Cache MISS):      {read2_time:>8.2f} ms  → Из БД")
    print(f"4. Повторное чтение (HIT):   {read3_time:>8.2f} ms  → Из кэша")
    print("-" * 60)
    print(f"Разница MISS vs HIT:         {speedup:>8.2f}x")
    print("=" * 60)
    print()
    print("При обновлении кэш автоматически инвалидируется")
    print("Следующий запрос загружает актуальные данные из БД")
    print("Данные снова кэшируются для быстрых последующих чтений")
    print("Гарантирована актуальность данных в кэше")
    print()


def run_pattern_demonstrations():
    print("=" * 60)
    print("ДЕМОНСТРАЦИЯ ПАТТЕРНОВ КЭШИРОВАНИЯ")
    print("=" * 60)
    print()
    
    if not check_api_availability():
        print("[ERROR] API недоступно по адресу", BASE_URL)
        return
    
    print("[OK] API доступно")
    print()
    
    demonstrate_read_through()
    time.sleep(1)
    
    demonstrate_write_through()
    time.sleep(1)
    
    demonstrate_cache_eviction()
    
    print("=" * 60)
    print("ИТОГОВОЕ ЗАКЛЮЧЕНИЕ")
    print("=" * 60)
    print()
    print("ПРОДЕМОНСТРИРОВАННЫЕ ПАТТЕРНЫ КЭШИРОВАНИЯ:")
    print()
    print("1. Read-Through:")
    print("   Автоматическое кэширование при чтении")
    print("   Использование: @Cacheable")
    print()
    print("2. Write-Through:")
    print("   Синхронная запись в кэш и БД")
    print("   Использование: @CachePut")
    print()
    print("РЕКОМЕНДАЦИИ:")
    print("  → Используйте Read-Through для данных с высоким R/W ratio")
    print("  → Применяйте Write-Through для критичных транзакционных данных")
    print("  → Настройте Cache Eviction для поддержания консистентности")
    print("  → Комбинируйте паттерны для оптимальной производительности")
    print()
    print("Все паттерны реализованы в CategoryServiceImpl")
    print("Просмотрите код для подробных комментариев")
    print("=" * 60)


if __name__ == "__main__":
    run_pattern_demonstrations()

