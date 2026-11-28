package ru.yandex.masterbot.adapter.yandex360.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO отдельного обновления, полученного от API Яндекс Мессенджера.
 *
 * @param updateId уникальный идентификатор обновления; в JSON передаётся как {@code update_id};
 *     используется для определения позиции при следующем запросе
 *     <p>
 * @param messageId идентификатор сообщения; в JSON передаётся как {@code message_id}
 *     <p>
 * @param timestamp время создания сообщения в формате Unix Timestamp (количество секунд с 1 января
 *     1970 года UTC)
 *     <p>
 * @param chat информация о чате, в котором было отправлено сообщение
 *     <p>
 * @param from информация об отправителе сообщения
 *     <p>
 * @param text текст сообщения; может отсутствовать, если обновление не содержит текстового
 *     сообщения
 */
public record YandexUpdate(
    @JsonProperty("update_id") Long updateId,
    @JsonProperty("message_id") Long messageId,
    Long timestamp,
    YandexChat chat,
    YandexUser from,
    String text) {}
