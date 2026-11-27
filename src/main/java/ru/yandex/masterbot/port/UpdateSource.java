package ru.yandex.masterbot.port;

import java.util.List;
import java.util.Objects;
import ru.yandex.masterbot.domain.IncomingEvent;

/** Источник событий; вызывается polling-циклом канала. Курсор сохраняет вызывающая сторона. */
public interface UpdateSource {

  /**
   * Возвращает события после непрозрачного строкового курсора и курсор следующей пачки. На первом
   * вызове передаётся пустая строка; адаптер не сохраняет курсор.
   *
   * @param cursor курсор канала; не {@code null}
   * @return события и курсор, который вызывающая сторона сохраняет после обработки пачки
   * @throws RuntimeException если получение обновлений завершилось ошибкой
   */
  Batch fetch(String cursor);

  record Batch(List<IncomingEvent> events, String nextCursor) {

    public Batch {
      events = List.copyOf(events);
      Objects.requireNonNull(nextCursor, "следующий курсор не должен быть null");
    }
  }
}
