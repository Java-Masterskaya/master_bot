# master_bot

Сервис приёма входящих заявок на проекты для команды аккаунт-менеджеров.
Заявки приходят с лендинга и через ботов в мессенджерах (Telegram, Яндекс 360),
каждая уходит на ИИ-оценку в отдельный Python-сервис через очередь NATS
JetStream и публикуется в чат менеджеров вместе с рекомендацией.

- [Техническое задание](docs/TZ.md)
- [Доска задач](https://github.com/orgs/Java-Masterskaya/projects/13)
- [Структура проекта](docs/project-structure.md)
- [Схема базы данных](docs/database-map.md)
- [Что уже реализовано](docs/implemented.md)
- [Как собрать и запустить](docs/build-and-run.md)
- [Что сделать перед коммитом](docs/before-committing.md)

Стек: Java 21, Spring Boot 3, Postgres, Flyway, NATS JetStream; оценщик —
Python 3.12, FastAPI.

