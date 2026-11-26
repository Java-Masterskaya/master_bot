package ru.yandex.masterbot.adapter.http.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/** Запрос на смену статуса заявки. */
@Schema(description = "Данные для смены статуса заявки")
public record TransitionRequest(
    @NotNull(message = "Статус обязателен")
        @Schema(
            description = "Новый статус заявки",
            example = "IN_REVIEW",
            requiredMode = Schema.RequiredMode.REQUIRED)
        TransitionStatus status,
    @Schema(description = "Комментарий к смене статуса", example = "Начали проверку заявки")
        String comment) {}
