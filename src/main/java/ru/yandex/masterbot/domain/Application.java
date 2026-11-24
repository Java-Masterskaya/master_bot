package ru.yandex.masterbot.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Агрегат заявки (подмножество таблицы {@code applications} из V1).
 *
 * <p>Содержит только то, что нужно для стейт-машины: идентификатор, номер, текущий статус, версию
 * (optimistic locking {@code version}) и историю переходов. Поля брифа, requester и raw_payload
 * сюда не входят — их маппинг живёт в infrastructure-слое и не влияет на правила переходов.
 *
 * <p>Класс неизменяемый: {@link #changeStatus} возвращает новый экземпляр, сохраняя версию и
 * историю предыдущего. Иммутабельность {@code history} гарантируется каноническим конструктором,
 * поэтому ни фабрики, ни прямой {@code new} не могут протащить мутабельный список.
 *
 * <p>Зависимостей от Lombok, Spring и JPA нет.
 */
public record Application(
    UUID id,
    long number,
    String source,
    ApplicationStatus status,
    long version,
    List<StatusChange> history) {

  /**
   * Канонический (компактный) конструктор — единственная точка валидации инвариантов.
   *
   * <p>Вызывается при любом {@code new Application(...)} и при десериализации. Обойти проверки
   * невозможно.
   */
  public Application {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(source, "source must not be null");
    Objects.requireNonNull(status, "status must not be null");

    if (number <= 0) {
      throw new IllegalArgumentException("number must be positive");
    }
    if (version < 0) {
      throw new IllegalArgumentException("version must be non-negative");
    }

    history = (history == null) ? List.of() : List.copyOf(history);
  }

  /** Создать новую заявку в статусе {@code NEW} с версией 0 и пустой историей. */
  public static Application create(UUID id, long number, String source) {
    return new Application(id, number, source, ApplicationStatus.NEW, 0L, List.of());
  }

  /**
   * Восстановить агрегат из персистентного состояния (без изменения версии). Используется
   * infrastructure-слоем при загрузке из БД.
   */
  public static Application restore(
      UUID id,
      long number,
      String source,
      ApplicationStatus status,
      long version,
      List<StatusChange> history) {
    return new Application(id, number, source, status, version, history);
  }

  /**
   * Изменить статус заявки.
   *
   * <p>Правила перехода проверяются через {@link ApplicationStatus#canTransitionTo}. При успехе
   * возвращается новый экземпляр с увеличенной на 1 версией и дополненной историей.
   *
   * @throws IllegalStatusTransitionException если переход запрещён правилами
   */
  public Application changeStatus(
      ApplicationStatus target, String actor, String comment, Instant at) {

    Objects.requireNonNull(target, "target status must not be null");
    Objects.requireNonNull(at, "timestamp must not be null");

    if (!this.status.canTransitionTo(target)) {
      throw new IllegalStatusTransitionException(this.status, target);
    }

    StatusChange change = new StatusChange(this.status, target, actor, comment, at);

    List<StatusChange> newHistory = new ArrayList<>(this.history.size() + 1);
    newHistory.addAll(this.history);
    newHistory.add(change);

    return new Application(id, number, source, target, this.version + 1, newHistory);
  }
}
