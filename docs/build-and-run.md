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
- задайте `DB_URL` с адресом вашей базы и `DB_USER` / `DB_PASSWORD` в окружении или используйте application-local.yaml
- переходите к шагу 1

0. Скопируйте `.env.example` в `.env`. Если у вас нет локально установленной
   СУБД PostgreSQL, запустите контейнеры PostgreSQL и NATS:
   ```bash
   docker compose up -d postgres nats
   ```
   Другие команды docker можно найти в документации к docker.  
   Управлять докером можно из Intellij Idea -> кнопка-Servises -> Docker.  
   Для подключения [docker-compose.yaml](../docker-compose.yaml) к Intellij Idea, откройте его и нажмите 
   кнопку Synchronyze в правом верхнем углу.

Для запуска приложения на хосте загрузите переменные в окружение перед
запуском приложения, как описано в [README](../README.md#конфигурация).

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
