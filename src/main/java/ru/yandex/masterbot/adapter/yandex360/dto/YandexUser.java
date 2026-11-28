package ru.yandex.masterbot.adapter.yandex360.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO информации о пользователе или боте, отправившем сообщение.
 *
 * @param id уникальный идентификатор пользователя
 *     <p>
 * @param displayName отображаемое имя пользователя; в JSON передаётся как {@code display_name}
 *     <p>
 * @param login логин пользователя в Яндекс Мессенджере
 *     <p>
 * @param robot признак того, что отправитель является ботом; {@code true} — бот, {@code false} —
 *     обычный пользователь
 */
public record YandexUser(
    String id, @JsonProperty("display_name") String displayName, String login, Boolean robot) {}
