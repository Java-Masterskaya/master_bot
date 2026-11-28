package ru.yandex.masterbot.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import ru.yandex.masterbot.port.UpdateSource;
import ru.yandex.masterbot.testing.FakeMessenger;

class FakeMessengerTest {

  @Test
  void processesIncomingTextAndRecordsReply() throws Exception {
    FakeMessenger messenger = new FakeMessenger();
    ChannelUserRef sender = new ChannelUserRef(Channel.TELEGRAM, "user-1", "chat-1", "Анна");
    IncomingEvent.Text event =
        new IncomingEvent.Text(Channel.TELEGRAM, "update-1", sender, "Хочу обсудить проект");
    messenger.addEvent(event);

    UpdateSource.Batch batch = messenger.fetch("0");
    for (IncomingEvent incomingEvent : batch.events()) {
      if (incomingEvent instanceof IncomingEvent.Text text) {
        messenger.send(
            new OutgoingMessage(
                new Target(text.channel(), text.from().chatId()),
                "Получено: " + text.text(),
                List.of(),
                "reply-" + text.updateId()));
      }
    }

    assertEquals(List.of(event), batch.events());
    assertEquals(1, messenger.sentMessages().size());
    assertEquals("Получено: Хочу обсудить проект", messenger.sentMessages().getFirst().text());
  }
}
