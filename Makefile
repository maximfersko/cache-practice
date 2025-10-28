.PHONY: help build start stop restart logs clean demo-baseline demo-cache demo-patterns demo-all test-api



demo-baseline:
	@echo "==============================================="
	@echo "ДЕМОНСТРАЦИЯ: БЕЗ кэширования"
	@echo "==============================================="
	@echo ""
	@python3 demo_1_baseline.py
	@echo ""
	@echo ""

demo-cache:
	@echo "==============================================="
	@echo "ДЕМОНСТРАЦИЯ: Кэширование"
	@echo "==============================================="
	@echo ""
	@python3 demo_2_with_cache.py
	@echo ""
	@echo "[NEXT] Запустите: make demo-patterns"
	@echo ""

demo-patterns: 
	@echo "==============================================="
	@echo "ДЕМОНСТРАЦИЯ 3: Паттерны кэширования"
	@echo "==============================================="
	@echo ""
	@python3 demo_3_patterns.py
	@echo ""
	@echo "[OK] Все демонстрации завершены"
	@echo ""


test-api:
	@echo "[TEST] Проверка доступности API"
	@curl -s -o /dev/null -w "HTTP Status: %{http_code}\n" http://localhost:8080/api/v1/categories || \
		echo "ERROR: API недоступно на http://localhost:8080"

redis-cli: 
	@echo "[REDIS] Подключение к Redis CLI"
	@echo "[INFO] Для выхода введите: quit"
	@echo ""
	docker exec -it cache-practice-redis redis-cli

psql: 
	@echo "[PSQL] Подключение к PostgreSQL"
	@echo "[INFO] Для выхода введите: \q"
	@echo ""
	docker exec -it cache-practice-postgres psql -U postgres -d cache_demo



present:
	@echo ""
	@echo "Нажмите Enter для начала..."
	@read dummy
	@echo ""
	@make demo-baseline
	@echo ""
	@echo "Нажмите Enter для следующей демонстрации..."
	@read dummy
	@echo ""
	@make demo-cache
	@echo ""
	@echo "Нажмите Enter для финальной демонстрации..."
	@read dummy
	@echo ""
	@make demo-patterns
	@echo ""
	@echo "==============================================="
	@echo "ПРЕЗЕНТАЦИЯ ЗАВЕРШЕНА"
	@echo "==============================================="



debug-cache: ## Просмотр содержимого Redis кэша
	@echo "[DEBUG] Содержимое Redis кэша"
	@echo ""
	@echo "Все ключи в Redis:"
	@docker exec cache-practice-redis redis-cli KEYS "*"
	@echo ""

debug-db:
	@echo "[DEBUG] Данные в PostgreSQL"
	@echo ""
	@docker exec cache-practice-postgres psql -U postgres -d cache_demo -c "\
		SELECT 'Categories' as table_name, COUNT(*) as count FROM category \
		UNION ALL \
		SELECT 'Products', COUNT(*) FROM product \
		UNION ALL \
		SELECT 'Prices', COUNT(*) FROM product_price \
		UNION ALL \
		SELECT 'Inventory', COUNT(*) FROM inventory \
		UNION ALL \
		SELECT 'Reviews', COUNT(*) FROM review;"


flush-cache:
	@echo "[FLUSH] Очистка Redis кэша"
	@docker exec cache-practice-redis redis-cli FLUSHALL
	@echo "[OK] Кэш очищен"

flush-db:
	@docker exec cache-practice-postgres psql -U postgres -d cache_demo -c "\
		TRUNCATE TABLE review CASCADE; \
		TRUNCATE TABLE inventory CASCADE; \
		TRUNCATE TABLE product_price CASCADE; \
		TRUNCATE TABLE product CASCADE; \
		TRUNCATE TABLE category CASCADE;"

reset: flush-cache flush-db 

