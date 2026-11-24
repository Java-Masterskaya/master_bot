package ru.yandex.masterbot.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Запись об одном переходе статуса заявки.
 *
 * <p>Соответствует строке таблицы {@code application_status_history} из миграции V1. Не зависит от
 * JPA — маппинг выполняется в infrastructure-слое.
 *
 * @param from статус до изменения (может быть {@code null} только для первого события, когда заявка
 *     создана; в текущем агрегате используется явный {@code NEW})
 * @param to статус после изменения; не {@code null}
 * @param actor кто инициировал изменение (например, {@code SYSTEM}, {@code ADMIN}, идентификатор
 *     пользователя); может быть {@code null}
 * @param comment причина/комментарий; может быть {@code null}
 * @param at момент изменения с часовым поясом; не {@code null}
 */
public record StatusChange(
    ApplicationStatus from, ApplicationStatus to, String actor, String comment, Instant at) {

  public StatusChange {
    Objects.requireNonNull(from, "from status must not be null");
    Objects.requireNonNull(to, "to status must not be null");
    Objects.requireNonNull(at, "timestamp must not be null");
    if (actor == null || actor.isBlank()) {
      throw new IllegalArgumentException("actor is required for every status change");
    }
  }
}
