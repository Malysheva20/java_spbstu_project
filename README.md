# Step 6: Implement Caching (Redis)

В этом шаге добавлено кэширование для ускорения работы с задачами.  
Используется Redis и Spring Cache. Кэширование включается через профиль `redis`.

## Основные изменения
- Добавлен профиль `redis` (`application-redis.properties`)
- Конфигурация Redis (`RedisConfig`) с настройкой TTL для кэшей:
  - `task:get` — 10 минут
  - `task:all` — 2 минуты
  - `task:pending` — 2 минуты
  - по умолчанию — 5 минут
- Реализован аспект `TaskCachingAspect`, который:
  - кэширует `get(id)`, `getAll(userId)`, `getPending(userId)`
  - инвалидирует кэш после создания или удаления задач
- Добавлен `docker-compose.redis.yml` для запуска приложения, PostgreSQL и Redis вместе
- В `pom.xml` добавлены зависимости для Redis и кэширования

## Зависимости для `pom.xml`
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

## Запуск локально
```bash
docker run -p 6379:6379 -d --name step6-redis redis:7-alpine redis-server --save ""
export SPRING_PROFILES_ACTIVE=pg,redis
export REDIS_HOST=localhost
export REDIS_PORT=6379
mvn spring-boot:run
```

## Запуск через Docker Compose
```bash
docker compose -f docker-compose.redis.yml up --build
```

## Проверка работы
1. Сделать запрос на получение задач пользователя:
   ```bash
   curl "http://localhost:8080/tasks?userId=1"
   ```
2. Повторить запрос и убедиться, что ответ берётся из Redis.
3. Проверить ключи в Redis:
   ```bash
   redis-cli keys "*task:*"
   redis-cli TTL "task:all::1"
   ```
4. Создать новую задачу:
   ```bash
   curl -X POST http://localhost:8080/tasks -H "Content-Type: application/json" -d '{"userId":1,"title":"Test"}'
   ```
   После этого кэш для списка задач пользователя будет сброшен.

