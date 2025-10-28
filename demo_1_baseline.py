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
    
    if not check_api_availability():
        print("[ERROR] API недоступно по адресу", BASE_URL)
        return

    print("[WAIT] Пауза 2 секунды для стабилизации БД")
    time.sleep(2)
    print()
    
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

