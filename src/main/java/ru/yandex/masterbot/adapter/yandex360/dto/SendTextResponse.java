package ru.yandex.masterbot.adapter.yandex360.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO ответа API на запрос отправки текстового сообщения.
 *
 * @param ok признак успешного выполнения операции; {@code true} — сообщение отправлено успешно
 *     <p>
 * @param messageId идентификатор отправленного сообщения; в JSON передаётся как {@code message_id};
 *     может отсутствовать при ошибке
 *     <p>
 * @param description описание ошибки, если операция завершилась неуспешно
 */
public record SendTextResponse(
    boolean ok, @JsonProperty("message_id") Long messageId, String description) {}
