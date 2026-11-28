package ru.yandex.masterbot.adapter.yandex360.dto;

import java.util.List;

/**
 * DTO ответа API на запрос получения обновлений (messages/getUpdates).
 *
 * @param ok признак успешного выполнения запроса
 *     <p>
 * @param updates список полученных обновлений
 *     <p>
 * @param description описание ошибки, если запрос завершился неуспешно
 */
public record UpdatesResponse(boolean ok, List<YandexUpdate> updates, String description) {}
