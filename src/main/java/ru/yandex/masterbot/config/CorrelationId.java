package ru.yandex.masterbot.config;

import java.util.UUID;
import org.slf4j.MDC;

/** Создаёт идентификаторы для независимых операций. */
public final class CorrelationId {
  public static final String MDC_KEY = "correlationId";

  private CorrelationId() {}

  public static String generate() {
    return UUID.randomUUID().toString();
  }

  /** Изолирует контекст фоновой операции и восстанавливает прежний контекст потока. */
  public static Runnable decorate(Runnable task) {
    return () -> {
      var previous = MDC.getCopyOfContextMap();
      try {
        MDC.clear();
        MDC.put(MDC_KEY, generate());
        task.run();
      } finally {
        MDC.clear();
        if (previous != null) {
          MDC.setContextMap(previous);
        }
      }
    };
  }
}
