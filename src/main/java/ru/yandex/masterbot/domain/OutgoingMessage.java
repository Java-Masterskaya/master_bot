package ru.yandex.masterbot.domain;

import java.util.List;
import java.util.Objects;

/** Сообщение ядра для отправки через канал. Ключ идемпотентности может быть {@code null}. */
public record OutgoingMessage(
    Target target, String text, List<List<Button>> buttons, String idempotencyKey) {

  public OutgoingMessage {
    Objects.requireNonNull(target, "адресат не должен быть null");
    Objects.requireNonNull(text, "текст сообщения не должен быть null");
    buttons = buttons.stream().map(List::copyOf).toList();
  }
}
