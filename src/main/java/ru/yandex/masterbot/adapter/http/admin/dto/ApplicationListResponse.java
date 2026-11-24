package ru.yandex.masterbot.adapter.http.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Ответ со списком заявок.
 *
 * <p>Структура ответа является предварительной и подлежит согласованию с командой.
 */
@Schema(description = "Страница заявок (предварительная структура)")
public record ApplicationListResponse(
    @Schema(description = "Элементы списка заявок") List<ApplicationListItemResponse> items,
    @Schema(
            description =
                "Курсор для получения следующей страницы; null, если следующей страницы нет",
            example = "eyJjcmVhdGVkQXQiOiIyMDI2LTExLTI0VDEwOjAwOjAwWiIsImlkIjoiLi4uIn0=")
        String nextCursor) {}
