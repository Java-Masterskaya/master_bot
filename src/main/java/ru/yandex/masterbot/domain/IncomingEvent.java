package ru.yandex.masterbot.domain;

import java.util.List;
import java.util.Objects;

public sealed interface IncomingEvent
    permits IncomingEvent.Text,
        IncomingEvent.ButtonPress,
        IncomingEvent.Command,
        IncomingEvent.Unsupported {

  Channel channel();

  String updateId();

  ChannelUserRef from();

  record Text(Channel channel, String updateId, ChannelUserRef from, String text)
      implements IncomingEvent {

    public Text {
      Objects.requireNonNull(channel, "канал не должен быть null");
      Objects.requireNonNull(updateId, "идентификатор обновления не должен быть null");
      Objects.requireNonNull(from, "отправитель не должен быть null");
      Objects.requireNonNull(text, "текст не должен быть null");
    }
  }

  record ButtonPress(
      Channel channel, String updateId, ChannelUserRef from, String actionId, String payload)
      implements IncomingEvent {

    public ButtonPress {
      Objects.requireNonNull(channel, "канал не должен быть null");
      Objects.requireNonNull(updateId, "идентификатор обновления не должен быть null");
      Objects.requireNonNull(from, "отправитель не должен быть null");
      Objects.requireNonNull(actionId, "идентификатор действия не должен быть null");
      Objects.requireNonNull(payload, "данные кнопки не должны быть null");
    }
  }

  record Command(
      Channel channel, String updateId, ChannelUserRef from, String name, List<String> args)
      implements IncomingEvent {

    public Command {
      Objects.requireNonNull(channel, "канал не должен быть null");
      Objects.requireNonNull(updateId, "идентификатор обновления не должен быть null");
      Objects.requireNonNull(from, "отправитель не должен быть null");
      Objects.requireNonNull(name, "имя команды не должно быть null");
      args = List.copyOf(args);
    }
  }

  record Unsupported(Channel channel, String updateId, ChannelUserRef from, String kind)
      implements IncomingEvent {

    public Unsupported {
      Objects.requireNonNull(channel, "канал не должен быть null");
      Objects.requireNonNull(updateId, "идентификатор обновления не должен быть null");
      Objects.requireNonNull(from, "отправитель не должен быть null");
      Objects.requireNonNull(kind, "тип события не должен быть null");
    }
  }
}
