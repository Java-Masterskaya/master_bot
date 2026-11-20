# Структура проекта

```
master_bot
├── src/
│   ├── main/java
│   │   ├── ru/yandex/masterbot
│   │   │   ├── adapter/                      # адаптеры наружного мира
│   │   │   │   ├── http/                     # REST-контроллеры и вебхуки
│   │   │   │   ├── nats/                     # обмен сообщениями через NATS
│   │   │   │   ├── persistence/              # репозитории, доступ к БД
│   │   │   │   ├── telegram/                 # интеграция с Telegram API
│   │   │   │   └── yandex360/                # интеграция с Yandex 360 API
│   │   │   ├── application/                  # слой приложения: use cases, сервисы
│   │   │   ├── config/                       # конфигурация Spring (бины, настройки)
│   │   │   ├── domain/                       # домен: сущности и бизнес-правила
│   │   │   └── port/                         # порты: интерфейсы входа и выхода
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/                 # миграции Flyway будут тут
│   └── test/
│       ├── java/com/example/masterbot/       # Тесты
│       └── resources/
│           └── application-test.yml
├── config/
│   └── checkstyle/                           # конфигурация Checkstyle
├── .github/
│   └── workflows/                            # GitHub Actions пайплайны
├── build.gradle.kts
├── settings.gradle.kts
├── docker-compose.yml
└── README.md
```
