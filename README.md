# Wordle Educational Platform - Backend

Backend для образовательной платформы изучения английского языка через игру Wordle.

## Технологии

- Spring Boot 3.0.4
- Kotlin
- PostgreSQL
- Redis
- Maven

## Требования

- Java 17
- Maven 3.6+
- PostgreSQL 15+

## Запуск локально

1. Убедитесь, что PostgreSQL запущен локально:
   ```bash
   # PostgreSQL должен быть доступен на localhost:5432
   ```

2. Настройте подключение к базе данных в `application.properties` (или через переменные окружения)

3. Запустите приложение

4. Приложение будет доступно на `http://localhost:8080`

## Запуск через Docker Compose

Для быстрого запуска всей инфраструктуры:

```bash
docker-compose up -d
```

Это запустит:
- PostgreSQL на порту 5432
- Backend на порту 8080

## Frontend

Frontend приложение находится в отдельном репозитории: [wordle-frontend-app](https://github.com/ksenideni/wordle-frontend-app)

Для полного запуска системы:
1. Запустите backend (локально или через Docker Compose)
2. Запустите frontend согласно инструкциям в его README