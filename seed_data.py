#!/usr/bin/env python3

import requests
import time
import random
from typing import List, Dict

BASE_URL = "http://localhost:8080"

def create_category(name: str, slug: str) -> Dict:
    response = requests.post(
        f"{BASE_URL}/api/v1/categories",
        json={"name": name, "slug": slug},
        timeout=10
    )
    if response.status_code == 201:
        return response.json()
    return None

def create_product(name: str, sku: str, description: str, category_id: str) -> Dict:
    response = requests.post(
        f"{BASE_URL}/api/v1/products",
        json={
            "name": name,
            "sku": sku,
            "description": description,
            "categoryId": category_id
        },
        timeout=10
    )
    if response.status_code == 201:
        return response.json()
    return None

def create_inventory(product_id: str, quantity: int, warehouse_id: int) -> Dict:
    response = requests.post(
        f"{BASE_URL}/api/v1/inventory",
        json={
            "productId": product_id,
            "quantity": quantity,
            "warehouseId": warehouse_id
        },
        timeout=10
    )
    if response.status_code == 201:
        return response.json()
    return None

def create_price(product_id: str, amount: float, currency: str) -> Dict:
    response = requests.post(
        f"{BASE_URL}/api/v1/product-prices",
        json={
            "productId": product_id,
            "amount": str(amount),
            "currency": currency
        },
        timeout=10
    )
    if response.status_code == 201:
        return response.json()
    return None

def create_review(product_id: str, rating: int, text: str) -> Dict:
    response = requests.post(
        f"{BASE_URL}/api/v1/reviews",
        json={
            "productId": product_id,
            "rating": rating,
            "text": text
        },
        timeout=10
    )
    if response.status_code == 201:
        return response.json()
    return None

def seed_database(num_categories: int = 50, products_per_category: int = 30):
    print("=" * 60)
    print("ЗАПОЛНЕНИЕ БАЗЫ ДАННЫХ ТЕСТОВЫМИ ДАННЫМИ")
    print("=" * 60)
    print(f"\nПлан:")
    print(f"   Категорий: {num_categories}")
    print(f"   Продуктов на категорию: {products_per_category}")
    print(f"   Общее количество продуктов: {num_categories * products_per_category}")
    print(f"   + Инвентарь, цены и отзывы для каждого продукта")
    print()
    
    start_time = time.time()
    
    categories_created = 0
    products_created = 0
    inventory_created = 0
    prices_created = 0
    reviews_created = 0
    
    print("Создание категорий...")
    category_ids = []
    
    category_names = [
        "Electronics", "Clothing", "Books", "Home & Garden", "Sports",
        "Toys", "Beauty", "Automotive", "Food", "Health",
        "Office Supplies", "Pet Supplies", "Tools", "Music", "Movies",
        "Video Games", "Furniture", "Jewelry", "Shoes", "Accessories"
    ]
    
    for i in range(num_categories):
        name = category_names[i % len(category_names)] + f" {i+1}"
        slug = f"category-{i+1}"
        
        category = create_category(name, slug)
        if category:
            category_ids.append(category['id'])
            categories_created += 1
            if (i + 1) % 5 == 0:
                print(f"   Создано категорий: {categories_created}/{num_categories}")
    
    print(f"Категорий создано: {categories_created}\n")
    
    print("Создание продуктов и связанных данных...")
    
    product_adjectives = ["Premium", "Deluxe", "Professional", "Classic", "Modern", "Vintage", "Ultimate", "Essential"]
    product_types = ["Widget", "Gadget", "Tool", "Device", "Item", "Product", "Solution", "System"]
    
    for cat_idx, category_id in enumerate(category_ids):
        for prod_idx in range(products_per_category):
            adj = random.choice(product_adjectives)
            type_name = random.choice(product_types)
            name = f"{adj} {type_name} {prod_idx+1}"
            sku = f"SKU-{cat_idx+1:03d}-{prod_idx+1:04d}"
            description = f"High-quality {type_name.lower()} with advanced features and excellent performance. " * 3
            
            product = create_product(name, sku, description, category_id)
            
            if product:
                product_id = product['id']
                products_created += 1
                
                warehouse_ids = [1, 2, 3]
                for warehouse_id in random.sample(warehouse_ids, random.randint(2, 3)):
                    inventory = create_inventory(
                        product_id,
                        quantity=random.randint(10, 500),
                        warehouse_id=warehouse_id
                    )
                    if inventory:
                        inventory_created += 1
                
                currencies = [("USD", 1.0), ("EUR", 0.85), ("GBP", 0.73)]
                base_price = random.uniform(9.99, 999.99)
                for currency, rate in random.sample(currencies, random.randint(2, 3)):
                    price = create_price(
                        product_id,
                        amount=round(base_price * rate, 2),
                        currency=currency
                    )
                    if price:
                        prices_created += 1
                
                reviews_texts = [
                    "Excellent product! Highly recommend.",
                    "Good quality for the price.",
                    "Works as expected, very satisfied.",
                    "Great purchase, would buy again.",
                    "Outstanding quality and fast delivery."
                ]
                
                for _ in range(random.randint(3, 5)):
                    review = create_review(
                        product_id,
                        rating=random.randint(3, 5),
                        text=random.choice(reviews_texts)
                    )
                    if review:
                        reviews_created += 1
        
        print(f"   Категория {cat_idx+1}/{len(category_ids)}: {products_per_category} продуктов + данные")
    
    elapsed_time = time.time() - start_time
    
    print()
    print("=" * 60)
    print("ЗАПОЛНЕНИЕ ЗАВЕРШЕНО")
    print("=" * 60)
    print(f"\nСоздано:")
    print(f"   Категорий:  {categories_created}")
    print(f"   Продуктов:  {products_created}")
    print(f"   Инвентаря:  {inventory_created}")
    print(f"   Цен:        {prices_created}")
    print(f"   Отзывов:    {reviews_created}")
    print(f"\nВремя выполнения: {elapsed_time:.2f} сек")
    print()
    print("Теперь запросы к БД будут более тяжелыми,")
    print("   и разница между Cache HIT и Cache MISS будет")
    print("   гораздо более заметной!")
    print()


if __name__ == "__main__":
    import sys
    
    num_cat = int(sys.argv[1]) if len(sys.argv) > 1 else 50
    prod_per_cat = int(sys.argv[2]) if len(sys.argv) > 2 else 30
    
    seed_database(num_cat, prod_per_cat)

