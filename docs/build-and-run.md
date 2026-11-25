# Конфигурация сборка и запуск

**Содержание:**

- [Конфигурация](#Конфигурация)
- [Сборка](#Сборка)
- [Запуск]()
    - [Запуск всего окружения в Docker](#запуск-всего-окружения-в-docker)
    - [Локальный запуск: NATS в Docker + приложение на хосте](#локальный-запуск-nats-в-docker--приложение-на-хосте)
        - [Вариант A. PostgreSQL установлен локально](#вариант-a-postgresql-установлен-локально)
        - [Вариант B. PostgreSQL в Docker](#вариант-b-postgresql-в-docker)
    - [Управление Docker](#управление-docker)
- [Проверка после запуска](#проверка-после-запуска)

---

## Конфигурация

Настройки `app` задаются переменными окружения; секреты не хранятся в файле
конфигурации. Для локального запуска скопируйте `.env.example` в `.env`,
укажите в `.env` секреты (пароль базы и необходимые токены) и другие нужные
значения, затем загрузите их в окружение shell:

```bash
cp .env.example .env # Скопировать файл-шаблон `.env.example` в рабочий файл `.env`
```

```bash
set -a # Включить режим добавления переменных окружения
source .env # Выполнить файл `.env` в текущей оболочке, загрузив из него переменные
set +a # Отключить режим добавления переменных окружения
```

Файл `.env` исключён из Git. Значение `DB_PASSWORD` в примере предназначено
только для локальной базы из `docker-compose.yaml`. Пустые токены допустимы,
пока соответствующий канал выключен. Для включённого канала токен обязателен.

| Ключ                             | Переменная окружения    | Значение по умолчанию                                          |
|----------------------------------|-------------------------|----------------------------------------------------------------|
| `app.db.url`                     | `DB_URL`                | `jdbc:postgresql://localhost:5433/master-bot?currentSchema=db` |
| `app.db.username`                | `DB_USER`               | `dbuser`                                                       |
| `app.db.password`                | `DB_PASSWORD`           | пусто                                                          |
| `app.admin.token`                | `ADMIN_TOKEN`           | пусто                                                          |
| `app.intake.token`               | `INTAKE_TOKEN`          | пусто                                                          |
| `app.nats.url`                   | `NATS_URL`              | `nats://localhost:4222`                                        |
| `app.channels.telegram.enabled`  | `TELEGRAM_ENABLED`      | `false`                                                        |
| `app.channels.telegram.token`    | `TELEGRAM_TOKEN`        | пусто                                                          |
| `app.channels.telegram.mode`     | `TELEGRAM_MODE`         | `polling`                                                      |
| `app.channels.yandex360.enabled` | `YANDEX360_ENABLED`     | `false`                                                        |
| `app.channels.yandex360.token`   | `YANDEX360_TOKEN`       | пусто                                                          |
| `app.channels.yandex360.mode`    | `YANDEX360_MODE`        | `polling`                                                      |
| `app.managers-chat.channel`      | `MANAGERS_CHAT_CHANNEL` | `yandex360`                                                    |
| `app.managers-chat.chat-id`      | `MANAGERS_CHAT_ID`      | пусто                                                          |
| `app.evaluation.enabled`         | `EVALUATION_ENABLED`    | `false`                                                        |

Режим канала: `polling` или `webhook`; реализация каналов появится в отдельных
задачах. Логи выводятся в JSON; HTTP-ответ содержит сгенерированный
`X-Correlation-ID`, а логи операции — поле `correlationId`. Фоновые задачи
стандартных исполнителя и планировщика Spring получают собственный ID при
каждом запуске. Для самостоятельно созданных потоков используйте
`CorrelationId.decorate`. Сервер завершает работу в режиме `graceful shutdown`.

---

## Сборка

```bash
# Сборка jar + все проверки
./gradlew build
```

```bash
# сборка без тестов (если нужно быстро)
./gradlew build -x test
```

Готовый артефакт: `build/libs/master-bot-<version>.jar`.

---

## Запуск проекта

---

### Запуск всего окружения в Docker

Compose запускает **PostgreSQL + NATS (JetStream) + приложение**. Для контейнера
приложения адреса базы и NATS задаются внутри сети Compose автоматически.
Здесь используется только `.env`, профиль `local` не нужен.

```bash
cp .env.example .env  # если файла .env ещё нет
```

```bash
docker compose up --build -d # Сборка и запуск контейнера
```

```bash
docker compose ps # Проверить что статус контейнера 'running'
```

> ⚠️ При изменении `DB_USER` или `DB_PASSWORD` существующий том PostgreSQL не
> переинициализируется: измените учётные данные в базе или сбросьте тома.
> См. [Управление Docker](#управление-docker)

Для сброса данных при новом запуске удобно использовать следующую команду:

```bash
docker compose up --remove-orphans   # перезапуск со сбросом старых данных (например после изменений в docker-compose.yaml)
```

---

### Локальный запуск: NATS в Docker + приложение на хосте

Приложение запускается на хосте с профилем `local` и использует
`application-local.yaml` — все адреса и учётные данные берутся из этого файла, **переменные окружения и `.env`
не нужны**. NATS поднимается в Docker;
PostgreSQL — либо локально установленный, либо в Docker (см. варианты ниже).

Запустите NATS (JetStream) в Docker:

```bash
docker compose up -d nats
```

#### Вариант A. PostgreSQL установлен локально

Убедитесь, что параметры подключения в `application-local.yaml` (`DB_URL`,
`DB_USER`, `DB_PASSWORD`) соответствуют вашей локальной базе.

#### Вариант B. PostgreSQL в Docker

```bash
docker compose up -d postgres
```

#### Запуск приложения с профилем `local`

Через Gradle:

```bash
./gradlew bootRun --args='--spring.profiles.active=local' --stacktrace
```

Или напрямую из IntelliJ IDEA (без Gradle):

1. **Run → Edit Configurations… → + → Spring Boot** (или **Application**).
2. Укажите главный класс приложения (например `MasterBotApplication`).
3. В поле **Program arguments** добавьте:

   ```
   --spring.profiles.active=local
   ```

Или через собранный jar:

```bash
java -jar build/libs/master_bot-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

---

### Управление Docker

**Управление мышкой:**

Управлять докером можно из Intellij Idea -> кнопка-Servises -> Docker.  
Для подключения [docker-compose.yaml](../docker-compose.yaml) к Intellij Idea, откройте его и нажмите
кнопку Synchronyze в правом верхнем углу.

**Управление командами:**

```bash
docker compose logs -f          # логи всех сервисов
```

```bash
docker compose logs -f app      # только приложение
```

```bash
docker compose down             # остановить, сохранив данные
```

```bash
docker compose down -v          # удалить также данные PostgreSQL и JetStream
```

```bash
docker compose up --remove-orphans   # перезапуск со сбросом старых данных (например после изменений в docker-compose.yaml)
```

---

## Проверка после запуска

**1. Проверка в логах**

`Tomcat started on port 8080` в JSON-логах означает, что веб-сервер запущен.
Gradle может продолжать показывать `80% EXECUTING` и `:bootRun` — это нормально,
пока приложение работает. Для остановки нажмите `Ctrl+C` в терминале, где
запущен `bootRun`.

**2. Проверка актуатора**

```bash
curl http://localhost:8080/actuator/health
# {"status":"UP"}
```

**3. Проверка сваггера**

Swagger UI доступен по адресу http://localhost:8080/swagger-ui.html.