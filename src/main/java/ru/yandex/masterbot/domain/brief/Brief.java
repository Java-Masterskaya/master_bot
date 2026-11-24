package ru.yandex.masterbot.domain.brief;

import java.util.Map;
import java.util.Objects;

/** Полный и валидный бриф проекта. */
public record Brief(Map<BriefField, String> answers) {
  public Brief {
    Objects.requireNonNull(answers, "answers");
    for (BriefField field : BriefField.values()) {
      if (!field.isValid(answers.get(field))) {
        throw new IllegalArgumentException(field.name() + ": " + field.errorHint());
      }
    }
    answers = Map.copyOf(answers);
  }
}
