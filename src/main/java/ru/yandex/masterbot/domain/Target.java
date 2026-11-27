package ru.yandex.masterbot.domain;

import java.util.Objects;

/** Адрес сообщения: канал и идентификатор чата, непрозрачный для ядра. */
public record Target(Channel channel, String chatId) {

  public Target {
    Objects.requireNonNull(channel, "канал не должен быть null");
    Objects.requireNonNull(chatId, "идентификатор чата не должен быть null");
  }
}
