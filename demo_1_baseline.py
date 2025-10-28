#!/usr/bin/env python3

import requests
import time
import statistics
from typing import List, Dict

BASE_URL = "http://localhost:8080"
ITERATIONS = 100


def check_api_availability() -> bool:
    try:
        response = requests.get(f"{BASE_URL}/api/v1/categories", timeout=5)
        return response.status_code < 500
    except requests.exceptions.RequestException:
        return False


def init_test_data() -> Dict:
    print("[INIT] Создание тестовых данных")
    print("-" * 60)
    
    session = requests.Session()
    categories = []
    
    # Создание категорий
    for i in range(5):
        category_data = {
            "name": f"Category {i+1}",
            "slug": f"category-{i+1}"
        }
        try:
            response = session.post(
                f"{BASE_URL}/api/v1/categories", 
                json=category_data,
                timeout=10
            )
            if response.status_code == 201:
                categories.append(response.json())
                print(f"[OK] Категория создана: {category_data['name']}")
        except Exception as e:
            print(f"[ERROR] Ошибка создания категории: {e}")
    
    # Создание продуктов
    products = []
    if categories:
        for i in range(20):
            import random
            category = random.choice(categories)
            product_data = {
                "sku": f"SKU-{i+1:03d}",
                "name": f"Product {i+1}",
                "description": f"Description for product {i+1}",
                "categoryId": category["id"]
            }
            try:
                response = session.post(
                    f"{BASE_URL}/api/v1/products",
                    json=product_data,
                    timeout=10
                )
                if response.status_code == 201:
                    products.append(response.json())
                    print(f"[OK] Продукт создан: {product_data['name']}")
            except Exception as e:
                print(f"[ERROR] Ошибка создания продукта: {e}")
    
    print("-" * 60)
    print(f"[INIT] Создано: {len(categories)} категорий, {len(products)} продуктов")
    print()
    
    return {"categories": categories, "products": products}


def measure_endpoint(endpoint: str, iterations: int) -> Dict:
    times_ms = []
    errors = 0
    session = requests.Session()
    
    for i in range(iterations):
        start = time.time()
        try:
            response = session.get(f"{BASE_URL}{endpoint}", timeout=10)
            if response.status_code >= 400:
                errors += 1
        except Exception:
            errors += 1
        end = time.time()
        
        elapsed_ms = (end - start) * 1000
        times_ms.append(elapsed_ms)
    
    # Расчет статистики
    times_sorted = sorted(times_ms)
    p95_index = int(len(times_sorted) * 0.95)
    
    return {
        "endpoint": endpoint,
        "iterations": iterations,
        "errors": errors,
        "avg_time_ms": round(statistics.mean(times_ms), 2),
        "median_time_ms": round(statistics.median(times_ms), 2),
        "min_time_ms": round(min(times_ms), 2),
        "max_time_ms": round(max(times_ms), 2),
        "p95_time_ms": round(times_sorted[p95_index], 2),
        "success_rate": round(((iterations - errors) / iterations) * 100, 2)
    }


def run_baseline_benchmark():
    print("=" * 60)
    print("BASELINE BENCHMARK - Производительность БЕЗ кэширования")
    print("=" * 60)
    print()
    
    # Проверка доступности API
    if not check_api_availability():
        print("[ERROR] API недоступно по адресу", BASE_URL)
        return
    
    print("[OK] API доступно")
    print()
    
    # Инициализация тестовых данных
    test_data = init_test_data()
    
    if not test_data["categories"]:
        print("[ERROR] Не удалось создать тестовые данные")
        return
    
    # Пауза для стабилизации БД
    print("[WAIT] Пауза 2 секунды для стабилизации БД")
    time.sleep(2)
    print()
    
    # Тестируемые endpoints
    endpoints = [
        "/api/v1/categories",
        "/api/v1/products"
    ]
    
    print("=" * 60)
    print("НАЧАЛО ЗАМЕРОВ")
    print("=" * 60)
    print(f"Количество запросов на endpoint: {ITERATIONS}")
    print()
    
    results = []
    total_start = time.time()
    
    for endpoint in endpoints:
        print(f"[TEST] Тестирование: {endpoint}")
        print("-" * 60)
        
        result = measure_endpoint(endpoint, ITERATIONS)
        results.append(result)
        
        print(f"Среднее время:        {result['avg_time_ms']:>8.2f} ms")
        print(f"Медианное время:      {result['median_time_ms']:>8.2f} ms")
        print(f"P95 время:            {result['p95_time_ms']:>8.2f} ms")
        print(f"Мин. время:           {result['min_time_ms']:>8.2f} ms")
        print(f"Макс. время:          {result['max_time_ms']:>8.2f} ms")
        print(f"Успешных запросов:    {result['success_rate']:>8.2f} %")
        print()
    
    total_end = time.time()
    total_time = total_end - total_start
    total_requests = sum(r["iterations"] for r in results)
    rps = total_requests / total_time
    
    print("=" * 60)
    print("ИТОГОВЫЕ РЕЗУЛЬТАТЫ")
    print("=" * 60)
    print(f"Всего запросов:       {total_requests:>8}")
    print(f"Общее время:          {total_time:>8.2f} s")
    print(f"Requests per second:  {rps:>8.2f} RPS")
    print()
    
    # Расчет средних метрик
    avg_time_all = statistics.mean([r["avg_time_ms"] for r in results])
    avg_p95_all = statistics.mean([r["p95_time_ms"] for r in results])
    
    print("УСРЕДНЕННЫЕ МЕТРИКИ:")
    print(f"Среднее время:        {avg_time_all:>8.2f} ms")
    print(f"Средний P95:          {avg_p95_all:>8.2f} ms")
    print()
    
    print("=" * 60)
    print("ЗАКЛЮЧЕНИЕ:")
    print("Baseline метрики зафиксированы.")
    print("Каждый запрос идет в БД (~50-100ms latency)")
    print("Нагрузка на БД: 100%")
    print("Следующий шаг: Запустите demo_2_with_cache.py")
    print("=" * 60)


if __name__ == "__main__":
    run_baseline_benchmark()

