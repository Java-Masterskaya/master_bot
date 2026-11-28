package ru.yandex.masterbot.adapter.yandex360.dto;

/**
 * DTO информации о чате, к которому относится сообщение.
 *
 * @param type тип чата, например {@code private} или {@code group}
 *     <p>
 * @param id уникальный идентификатор чата
 */
public record YandexChat(String type, String id) {}
