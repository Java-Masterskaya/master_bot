# Контракт очереди оценки (EVAL)

Общий контракт Java и Python для асинхронной оценки заявок через NATS JetStream. Версия контракта: **1.0**

## Топология JetStream

| Параметр | Значение |
|---|---|
| Stream | EVAL|
| Subject| `eval.requests.v1`, `eval.results.v1`|
| Storage| File|
| Retention| Limits (MaxAge = 7d)|
| Duplicate window| 10 минут |
| Nats-Msg-Id|`jobId` (UUID) для обоих subject|

### Durable consumers

| Consumer | Subject | AckWait | MaxDeliver | Назначение |
|---|---|---|---|---|
| `evaluator` | `eval.requests.v1` | 300 s | 3 | Python-оценщик забирает запросы |
| `results` | `eval.results.v1` | 300 s | 3 | Java забирает результаты |


## Поток сообщений

```
Java ──[tx]──▶ outbox ──[worker]──▶ JetStream (EVAL)
                                        │
                                eval.requests.v1
                                        │
                                        ▼
                                consumer "evaluator"
                                        │
                                        ▼
                                  Python-оценщик
                                        │
                                  eval.results.v1
                                        │
                                        ▼
                                   consumer "results"
                                        │
                                        ▼
                              Java получает сообщение
                                        ├─▶ 1. дедуп по jobId: проверка evaluation_jobs
                                        ├─▶ 2. INSERT в evaluation_results
                                        ├─▶ 3. UPDATE evaluation_jobs
                                        └─▶ 4. INSERT в outbox
```

## Гарантии

- **At-least-once** на обоих переходах: outbox → JetStream и JetStream → consumer.
- **Дедупликация публикации** — JetStream по `Nats-Msg-Id = jobId` в окне 10 мин.
- **Дедупликация результатов в Java** — уникальный ключ `evaluation_results.job_id`
  (PK по `job_id` в V1) + проверка статуса `evaluation_jobs` перед записью.
- **Idempotency на стороне Python** — повторная обработка того же `jobId` должна
  возвращать тот же результат (обязательно при `MaxDeliver > 1`).
- **Exactly-once не гарантируется**: результат должен быть идемпотентен по `jobId`.

## Совместимость и изменения

- `schemaVersion` — SemVer-строка, текущая `"1.0"`.
- Любое breaking change → новый subject (`eval.requests.v2`, `eval.results.v2`)
  и новая пара схем. Старые subjects живут, пока есть consumer'ы.
- Аддитивные (не-breaking) изменения → минорная версия схемы, поле остаётся
  необязательным; оба конца игнорируют неизвестные поля (`additionalProperties: false`
  в схеме — только для валидации в тестах, в рантайме используется lenient-парсер).

## Файлы

- `eval.requests.v1.json` — схема запроса
- `eval.results.v1.json` — схема результата
- `examples/request.success.json` — успешный запрос
- `examples/result.success.json` — успешный результат
- `examples/result.degraded.json` — результат в fallback-режиме
- `examples/result.failed.json` — результат с ошибкой