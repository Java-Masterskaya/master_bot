package ru.yandex.masterbot.adapter.http.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Полная карточка заявки для административного API. */
@Schema(description = "Полная информация о заявке")
public record ApplicationDetailsResponse(
    @Schema(description = "Уникальный идентификатор заявки") UUID id,
    @Schema(description = "Номер заявки", example = "10042") long number,
    @Schema(description = "Источник заявки", example = "LANDING") String source,
    @Schema(description = "Информация о заявителе") RequesterInfo requester,
    @Schema(description = "Поля брифа заявки") Brief brief,
    @Schema(description = "Исходные данные запроса") Map<String, Object> rawPayload,
    @Schema(description = "Текущий статус заявки", example = "IN_REVIEW") String status,
    @Schema(description = "История изменения статусов") List<StatusHistoryItem> statusHistory,
    @Schema(description = "Результат последней оценки заявки") EvaluationDetails evaluation,
    @Schema(description = "Версия заявки для контроля конкурентных изменений") long version,
    @Schema(description = "Дата и время создания заявки") Instant createdAt,
    @Schema(description = "Дата и время последнего обновления заявки") Instant updatedAt) {

  /** Информация о пользователе, отправившем заявку. */
  @Schema(description = "Заявитель")
  public record RequesterInfo(
      @Schema(description = "Идентификатор пользователя", example = "123456") String userId,
      @Schema(description = "Логин заявителя", example = "ivanov") String login,
      @Schema(description = "Отображаемое имя заявителя", example = "Иван Иванов")
          String displayName,
      @Schema(description = "Идентификатор чата заявителя", example = "123456") String chatId) {}

  /** Основные поля брифа. */
  @Schema(description = "Данные брифа")
  public record Brief(
      @Schema(description = "Название проекта", example = "CRM для отдела продаж") String title,
      @Schema(description = "Организация-заказчик", example = "ООО Ромашка") String customer,
      @Schema(description = "Описание проекта") String description,
      @Schema(description = "Ожидаемый результат") String expectedResult,
      @Schema(description = "Технологический стек", example = "Java, Spring Boot") String stack,
      @Schema(description = "Сроки реализации", example = "3 месяца") String deadlines,
      @Schema(description = "Контакт заказчика", example = "manager@example.com")
          String customerContact) {}

  /** Запись об одном изменении статуса заявки. */
  @Schema(description = "Элемент истории изменения статусов")
  public record StatusHistoryItem(
      @Schema(description = "Предыдущий статус", example = "NEW") String fromStatus,
      @Schema(description = "Новый статус", example = "IN_REVIEW") String toStatus,
      @Schema(description = "Комментарий к переходу") String comment,
      @Schema(description = "Актор, выполнивший переход", example = "manager") String actor,
      @Schema(description = "Дата и время изменения статуса") Instant createdAt) {}

  /** Информация о результате оценки заявки. */
  @Schema(description = "Результат автоматической оценки")
  public record EvaluationDetails(
      @Schema(description = "Статус задания оценки", example = "COMPLETED") String status,
      @Schema(description = "Рекомендация по заявке", example = "ACCEPT") String recommendation,
      @Schema(description = "Причины вынесенной рекомендации") List<String> reasons,
      @Schema(description = "Оценки отдельных задач") Map<String, Object> taskEvaluations,
      @Schema(description = "Вопросы, требующие уточнения") List<String> questions,
      @Schema(description = "Черновик ответа заявителю") String draftResponse,
      @Schema(description = "Признак использования упрощённого режима оценки") Boolean degraded,
      @Schema(description = "Версия оценки") Long version,
      @Schema(description = "Количество входных токенов") Long inputTokens,
      @Schema(description = "Количество выходных токенов") Long outputTokens,
      @Schema(description = "Общее количество использованных токенов") Long totalTokens) {}
}
