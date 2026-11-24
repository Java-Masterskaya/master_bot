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


## Python-оценщик

Исходники команды DS подключены как Git-сабмодуль на коммите `b76cbd1`.
У них нет конфигурации для установки Python-пакета, поэтому образ берёт
исходники из сабмодуля и устанавливает зависимости из его `requirements.txt`.

```bash
git submodule update --init --recursive
docker compose up --build --wait evaluator
curl http://localhost:8000/healthz
# {"status":"ok"}
```

При необходимости укажите `KODIK_API_KEY` в локальном `.env`. Compose передаёт
его только контейнеру оценщика. Для проверки `/healthz` ключ не требуется.

Чтобы запустить проверки локально, активируйте виртуальное окружение
с Python 3.12 и выполните из корня репозитория:

```bash
cd evaluator
python -m pip install -r vendor/brief-evaluator/requirements.txt '.[dev]'
python -m ruff check
python -m pytest
```

## Конфигурация

Настройки `app` задаются переменными окружения; секреты не хранятся в файле
конфигурации. Для локального запуска скопируйте `.env.example` в `.env`,
укажите в `.env` секреты (пароль базы и необходимые токены) и другие нужные
значения, затем загрузите их в окружение shell:

```bash
cp .env.example .env
set -a
source .env
set +a
```

Файл `.env` исключён из Git. Значение `DB_PASSWORD` в примере предназначено
только для локальной базы из `docker-compose.yaml`. Пустые токены допустимы,
пока соответствующий канал выключен. Для включённого канала токен обязателен.

| Ключ | Переменная окружения | Значение по умолчанию |
| --- | --- | --- |
| `app.db.url` | `DB_URL` | `jdbc:postgresql://localhost:5433/master-bot?currentSchema=db` |
| `app.db.username` | `DB_USER` | `dbuser` |
| `app.db.password` | `DB_PASSWORD` | пусто |
| `app.admin.token` | `ADMIN_TOKEN` | пусто |
| `app.intake.token` | `INTAKE_TOKEN` | пусто |
| `app.nats.url` | `NATS_URL` | `nats://localhost:4222` |
| `app.channels.telegram.enabled` | `TELEGRAM_ENABLED` | `false` |
| `app.channels.telegram.token` | `TELEGRAM_TOKEN` | пусто |
| `app.channels.telegram.mode` | `TELEGRAM_MODE` | `polling` |
| `app.channels.yandex360.enabled` | `YANDEX360_ENABLED` | `false` |
| `app.channels.yandex360.token` | `YANDEX360_TOKEN` | пусто |
| `app.channels.yandex360.mode` | `YANDEX360_MODE` | `polling` |
| `app.managers-chat.channel` | `MANAGERS_CHAT_CHANNEL` | `yandex360` |
| `app.managers-chat.chat-id` | `MANAGERS_CHAT_ID` | пусто |
| `app.evaluation.enabled` | `EVALUATION_ENABLED` | `false` |

Режим канала: `polling` или `webhook`; реализация каналов появится в отдельных
задачах. Логи выводятся в JSON; HTTP-ответ содержит сгенерированный
`X-Correlation-ID`, а логи операции — поле `correlationId`. Фоновые задачи
стандартных исполнителя и планировщика Spring получают собственный ID при
каждом запуске. Для самостоятельно созданных потоков используйте
`CorrelationId.decorate`. Сервер завершает работу в режиме graceful shutdown.


## Проверка запуска

При запуске через `./gradlew bootRun` сообщение `Tomcat started on port 8080`
в JSON-логах означает, что веб-сервер запущен. Gradle может продолжать
показывать `80% EXECUTING` и `:bootRun`: это нормально, пока приложение работает.

Откройте [проверку состояния](http://localhost:8080/actuator/health) в браузере
или выполните в другом терминале:

```bash
curl http://localhost:8080/actuator/health
# {"status":"UP"}
```

Для остановки приложения нажмите `Ctrl+C` в терминале, где запущен `bootRun`.
