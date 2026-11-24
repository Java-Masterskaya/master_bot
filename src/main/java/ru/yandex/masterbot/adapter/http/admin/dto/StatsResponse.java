package ru.yandex.masterbot.adapter.http.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;

/** Сводная статистика по заявкам и результатам их оценки. */
@Schema(description = "Сводная статистика административной панели")
public record StatsResponse(
    @Schema(
            description = "Количество заявок по статусам",
            example = "{\"NEW\": 12, \"IN_REVIEW\": 5, \"ACCEPTED\": 8, \"REJECTED\": 3}")
        Map<String, Long> applicationsByStatus,
    @Schema(
            description = "Количество заявок по источникам",
            example = "{\"LANDING\": 15, \"TELEMOST\": 13}")
        Map<String, Long> applicationsBySource,
    @Schema(description = "Количество заявок за последние 7 дней", example = "12")
        long applicationsLast7Days,
    @Schema(description = "Количество заявок за последние 30 дней", example = "48")
        long applicationsLast30Days,
    @Schema(description = "Топ заявителей по количеству заявок") List<RequesterStats> topRequesters,
    @Schema(
            description = "Медианное время от создания заявки до принятия решения, в секундах",
            example = "86400")
        Long medianTimeToDecisionSeconds,
    @Schema(
            description = "Распределение заявок по рекомендациям",
            example = "{\"ACCEPT\": 10, \"REJECT\": 4, \"CLARIFY\": 6}")
        Map<String, Long> applicationsByRecommendation,
    @Schema(description = "Общее количество использованных токенов", example = "125000")
        long totalTokens) {

  /** Статистика по отдельному заявителю. */
  @Schema(description = "Количество заявок отдельного заявителя")
  public record RequesterStats(
      @Schema(description = "Логин заявителя", example = "ivanov") String requesterLogin,
      @Schema(description = "Количество заявок", example = "7") long applicationsCount) {}
}
