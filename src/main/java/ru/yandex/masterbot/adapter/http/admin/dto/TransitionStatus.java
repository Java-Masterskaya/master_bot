package ru.yandex.masterbot.adapter.http.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Допустимые целевые статусы заявки при смене статуса через Admin API. */
@Schema(description = "Целевой статус заявки")
public enum TransitionStatus {
  IN_REVIEW,
  ACCEPTED,
  REJECTED
}
