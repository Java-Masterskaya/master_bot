package ru.yandex.masterbot.port;

/** Классифицирует ошибки отправки; вызывается outbox-доставщиком после сбоя. */
public interface SendErrorClassifier {

  /**
   * Возвращает {@link SendFailureKind#TRANSIENT} для ошибки, допускающей повтор, или {@link
   * SendFailureKind#PERMANENT} для окончательного отказа.
   *
   * @param exception ошибка, полученная при отправке
   * @return категория ошибки
   * @throws NullPointerException если передано {@code null}
   */
  SendFailureKind classify(MessengerSendException exception);
}
