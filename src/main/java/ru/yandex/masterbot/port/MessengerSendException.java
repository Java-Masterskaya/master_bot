package ru.yandex.masterbot.port;

/** Ошибка адаптера, возникшая при отправке сообщения. */
public class MessengerSendException extends Exception {

  public MessengerSendException(String message, Throwable cause) {
    super(message, cause);
  }
}
