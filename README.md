# Step 7: Implement Messaging (RabbitMQ)

В этом шаге добавлена интеграция с RabbitMQ.  
Теперь уведомления создаются **только** через брокер сообщений, а не напрямую из сервисов.

## Основные изменения
- Добавлен профиль `amqp` (`application-amqp.properties`).
- Конфигурация `RabbitConfig` с:
  - Exchange: `task.events`
  - Routing key: `task.created`
  - Queue: `notifications.task.created`
- Аспект `TaskCreatePublishAspect` публикует событие `TaskCreatedEvent` при создании новой задачи.
- Слушатель `NotificationEventListener` принимает события из очереди и сохраняет уведомления в БД.
- Добавлен `docker-compose.rabbit.yml` для запуска PostgreSQL, RabbitMQ и приложения.

## Зависимости для `pom.xml`
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

## Запуск локально
Запуск RabbitMQ:
```bash
docker run -d --name step7-rabbit -p 5672:5672 -p 15672:15672 rabbitmq:3.13-management
```
Задать профиль и переменные окружения:
```bash
export SPRING_PROFILES_ACTIVE=pg,amqp
export RABBIT_HOST=localhost
export RABBIT_PORT=5672
export RABBIT_USER=guest
export RABBIT_PASS=guest
mvn spring-boot:run
```

## Запуск через Docker Compose
```bash
docker compose -f docker-compose.rabbit.yml up --build
```

## Проверка работы
1. Создать задачу:
   ```bash
   curl -X POST http://localhost:8080/tasks -H "Content-Type: application/json" -d '{"userId":1,"title":"Test"}'
   ```
2. Проверить очередь RabbitMQ через веб-интерфейс: http://localhost:15672 (guest/guest).
3. Убедиться, что уведомление появилось в БД (через endpoint `/notifications`).

