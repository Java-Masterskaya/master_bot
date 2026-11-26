package ru.yandex.masterbot.application;

/** Информация об инициаторе заявки. */
public record Requester(String channel, String userId, String chatId, String name) {}
