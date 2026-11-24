package ru.yandex.masterbot.domain;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Бизнес-статус заявки.
 *
 * <p>Правила переходов живут ровно в одном месте — карте {@link #ALLOWED_TRANSITIONS}. Таблица
 * переходов соответствует миграции V1__init_schema.sql:
 *
 * <pre>
 *   NEW       -> IN_REVIEW
 *   IN_REVIEW -> ACCEPTED | REJECTED
 *   ACCEPTED  -> (терминальный)
 *   REJECTED  -> (терминальный)
 * </pre>
 *
 * <p>Этот enum не зависит ни от Spring, ни от JPA, ни от базы данных.
 */
public enum ApplicationStatus {
  NEW,
  IN_REVIEW,
  ACCEPTED,
  REJECTED;

  /**
   * Таблица разрешённых переходов. Строится один раз при инициализации класса. Значения —
   * неизменяемые множества ({@link EnumSet#clone()} снимает мутабельность).
   */
  private static final Map<ApplicationStatus, Set<ApplicationStatus>> ALLOWED_TRANSITIONS;

  static {
    Map<ApplicationStatus, Set<ApplicationStatus>> map = new EnumMap<>(ApplicationStatus.class);
    map.put(NEW, EnumSet.of(IN_REVIEW));
    map.put(IN_REVIEW, EnumSet.of(ACCEPTED, REJECTED));
    map.put(ACCEPTED, EnumSet.noneOf(ApplicationStatus.class));
    map.put(REJECTED, EnumSet.noneOf(ApplicationStatus.class));

    Map<ApplicationStatus, Set<ApplicationStatus>> frozen = new EnumMap<>(ApplicationStatus.class);
    map.forEach((status, targets) -> frozen.put(status, Collections.unmodifiableSet(targets)));
    ALLOWED_TRANSITIONS = Collections.unmodifiableMap(frozen);
  }

  /**
   * Можно ли из текущего статуса перейти в {@code target}.
   *
   * @param target целевой статус; не {@code null}
   * @return {@code true}, если переход разрешён правилами домена
   * @throws NullPointerException если {@code target == null}
   */
  public boolean canTransitionTo(ApplicationStatus target) {
    Objects.requireNonNull(target, "target status must not be null");
    return ALLOWED_TRANSITIONS.get(this).contains(target);
  }

  /** Является ли статус терминальным (из него нет ни одного разрешённого перехода). */
  public boolean isTerminal() {
    return ALLOWED_TRANSITIONS.get(this).isEmpty();
  }
}
