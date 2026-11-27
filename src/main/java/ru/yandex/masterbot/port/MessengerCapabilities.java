package ru.yandex.masterbot.port;

/** Описывает ограничения канала; вызывается ядром при подготовке сообщения. */
public interface MessengerCapabilities {

  /** Максимальная длина текста, принимаемая каналом. */
  int maxTextLength();

  /** Поддерживает ли канал кнопки. */
  boolean supportsButtons();

  /** Поддерживает ли канал ключи идемпотентности при отправке. */
  boolean supportsIdempotency();
}
