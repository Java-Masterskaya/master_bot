package ru.yandex.masterbot.adapter.http.intake.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/** Ответ на приём новой заявки. */
@Schema(description = "Результат приёма заявки")
public record IntakeApplicationResponse(
    @Schema(description = "UUID созданной заявки", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
    @Schema(description = "Номер созданной заявки", example = "10042") long number) {}
