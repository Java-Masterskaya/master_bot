package ru.yandex.masterbot.adapter.yandex360.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO запроса на отправку текстового сообщения в Яндекс Мессенджере.
 *
 * @param chatId идентификатор чата, в который необходимо отправить сообщение; в JSON передаётся как
 *     {@code chat_id}
 *     <p>
 * @param text текст отправляемого сообщения
 */
public record SendTextRequest(@JsonProperty("chat_id") String chatId, String text) {}
