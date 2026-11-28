package ru.yandex.masterbot.domain;

import java.util.Objects;

public record Button(String actionId, String title, String payload) {

  public Button {
    Objects.requireNonNull(actionId, "идентификатор действия не должен быть null");
    Objects.requireNonNull(title, "название кнопки не должно быть null");
    Objects.requireNonNull(payload, "данные кнопки не должны быть null");
  }
}
