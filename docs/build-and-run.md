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

1. Запуск через Gradle:
   ```bash
   ./gradlew bootRun
   ```
2. Или через собранный jar:
   ```bash
   java -jar build/libs/master-bot-<version>.jar
   ```
3. Проверка здоровья:
   ```bash
   curl http://localhost:8080/actuator/health
    # {"status":"UP"}
   ```
