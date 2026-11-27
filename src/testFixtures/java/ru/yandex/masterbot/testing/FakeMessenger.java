package ru.yandex.masterbot.testing;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import ru.yandex.masterbot.domain.IncomingEvent;
import ru.yandex.masterbot.domain.OutgoingMessage;
import ru.yandex.masterbot.port.MessengerCapabilities;
import ru.yandex.masterbot.port.MessengerSendException;
import ru.yandex.masterbot.port.MessengerSender;
import ru.yandex.masterbot.port.SendErrorClassifier;
import ru.yandex.masterbot.port.SendFailureKind;
import ru.yandex.masterbot.port.UpdateSource;

public class FakeMessenger
    implements UpdateSource, MessengerSender, MessengerCapabilities, SendErrorClassifier {

  private static final int DEFAULT_MAX_TEXT_LENGTH = 4096;

  private final Queue<IncomingEvent> events = new ArrayDeque<>();
  private final List<OutgoingMessage> sentMessages = new ArrayList<>();
  private MessengerSendException sendFailure;
  private SendFailureKind sendFailureKind = SendFailureKind.PERMANENT;
  private long cursor;

  public synchronized void addEvent(IncomingEvent event) {
    events.add(Objects.requireNonNull(event, "событие не должно быть null"));
  }

  @Override
  public synchronized Batch fetch(String currentCursor) {
    List<IncomingEvent> batch = new ArrayList<>(events);
    events.clear();
    cursor++;
    return new Batch(batch, Long.toString(cursor));
  }

  @Override
  public synchronized void send(OutgoingMessage message) throws MessengerSendException {
    if (sendFailure != null) {
      throw sendFailure;
    }
    sentMessages.add(Objects.requireNonNull(message, "сообщение не должно быть null"));
  }

  public synchronized List<OutgoingMessage> sentMessages() {
    return List.copyOf(sentMessages);
  }

  public synchronized void failSendingWith(
      MessengerSendException exception, SendFailureKind failureKind) {
    sendFailure = Objects.requireNonNull(exception, "ошибка отправки не должна быть null");
    sendFailureKind = Objects.requireNonNull(failureKind, "тип ошибки не должен быть null");
  }

  public synchronized void allowSending() {
    sendFailure = null;
    sendFailureKind = SendFailureKind.PERMANENT;
  }

  public synchronized void clear() {
    events.clear();
    sentMessages.clear();
    allowSending();
    cursor = 0;
  }

  @Override
  public int maxTextLength() {
    return DEFAULT_MAX_TEXT_LENGTH;
  }

  @Override
  public boolean supportsButtons() {
    return true;
  }

  @Override
  public boolean supportsIdempotency() {
    return true;
  }

  @Override
  public synchronized SendFailureKind classify(MessengerSendException exception) {
    Objects.requireNonNull(exception, "ошибка отправки не должна быть null");
    return exception == sendFailure ? sendFailureKind : SendFailureKind.PERMANENT;
  }
}
