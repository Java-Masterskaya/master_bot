package ru.yandex.masterbot.domain;

import java.util.Objects;

/** Платформенно-независимая ссылка на пользователя и его чат в мессенджере. */
public record ChannelUserRef(Channel channel, String userId, String chatId, String displayName) {

  public ChannelUserRef {
    Objects.requireNonNull(channel, "канал не должен быть null");
    Objects.requireNonNull(userId, "идентификатор пользователя не должен быть null");
    Objects.requireNonNull(chatId, "идентификатор чата не должен быть null");
  }
}
