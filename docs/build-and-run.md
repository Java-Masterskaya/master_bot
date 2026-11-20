# Сборка и запуск

## Сборка

```bash
./gradlew build          # сборка jar + все проверки
```

```bash
./gradlew build -x test  # сборка без тестов (если нужно быстро)
```
Готовый артефакт: build/libs/master-bot-<version>.jar.

## Локальный запуск

**Подготовка к запуску:**

Если вы используете локально установленную СУБД postgreSQL 
- установите в application.yaml порт для подключения к вашей базе (замените `DB_PORT:5433` на `DB_PORT:ваш_порт`)
- переходите к шагу 1

0. Если у вас нет локально установленной СУБД postgreSQL запустите docker-контейнер с postgres 16.1
   ```bash
   docker compose up -d
   ```

**Сам запуск**

1. Запуск через Gradle:
   ```bash
   ./gradlew bootRun
   ```
2. Или через собранный jar:
   ```bash
   java -jar build/libs/master_bot-0.0.1-SNAPSHOT.jar
   ```
3. Проверка здоровья:
   ```bash
   curl http://localhost:8080/actuator/health
    # {"status":"UP"}
   ```
