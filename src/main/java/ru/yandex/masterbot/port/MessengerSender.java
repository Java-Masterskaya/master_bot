package ru.yandex.masterbot.port;

import ru.yandex.masterbot.domain.OutgoingMessage;

/** Отправляет сообщения; вызывается ядром через outbox-доставщик. */
public interface MessengerSender {

  /**
   * Отправляет сообщение по указанному каналу и адресу.
   *
   * @param message сообщение для отправки
   * @throws MessengerSendException если канал не смог отправить сообщение
   */
  void send(OutgoingMessage message) throws MessengerSendException;
}
