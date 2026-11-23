package ru.yandex.masterbot.domain;

import java.util.Objects;

/**
 * Нарушение правил перехода статусов заявки.
 *
 * <p>Доменное исключение: не наследует ни Spring, ни JPA-исключения. Runtime, чтобы не загрязнять
 * сигнатуры методов агрегата.
 */
public class IllegalStatusTransitionException extends RuntimeException {

  private final ApplicationStatus from;
  private final ApplicationStatus to;

  public IllegalStatusTransitionException(ApplicationStatus from, ApplicationStatus to) {
    super("Illegal status transition: " + from + " -> " + to);
    this.from = Objects.requireNonNull(from, "from status must not be null");
    this.to = Objects.requireNonNull(to, "to status must not be null");
  }

  public ApplicationStatus from() {
    return from;
  }

  public ApplicationStatus to() {
    return to;
  }
}
