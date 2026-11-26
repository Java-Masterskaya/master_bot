package ru.yandex.masterbot.adapter.http.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

/**
 * Элемент списка заявок.
 *
 * <p>Состав полей является предварительным и подлежит согласованию с командой.
 */
@Schema(description = "Элемент списка заявок (предварительная структура)")
public record ApplicationListItemResponse(
    @Schema(description = "Уникальный идентификатор заявки") UUID id,
    @Schema(description = "Номер заявки", example = "123") long number,
    @Schema(description = "Название заявки", example = "CRM для отдела продаж") String title,
    @Schema(description = "Источник заявки", example = "LANDING") String source,
    @Schema(description = "Текущий статус заявки", example = "NEW") String status,
    @Schema(description = "Логин заявителя", example = "ivanov") String requesterLogin,
    @Schema(description = "Дата и время создания заявки", example = "2026-11-24T10:00:00Z")
        Instant createdAt) {}
