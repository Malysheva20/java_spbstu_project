# Step 8: Scheduling & Async

Профиль `sched` включает планировщик и асинхронную обработку. Периодически ищутся просроченные задачи и создаются уведомления.

## Что добавлено
- `application-sched.properties`
- `SchedulingConfig`
- `OverdueTaskNativeRepository`
- `OverdueTaskScheduler`
- `docker-compose.sched.yml`

## Запуск
```bash
docker compose -f docker-compose.sched.yml up --build
```
Профили: `SPRING_PROFILES_ACTIVE=pg,sched`.

## Настройки
`app.scheduling.overdue.fixedDelay` — период проверки в миллисекундах (по умолчанию 60000).

