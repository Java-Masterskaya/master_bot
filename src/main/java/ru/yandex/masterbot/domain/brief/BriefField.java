package ru.yandex.masterbot.domain.brief;

import java.util.function.Predicate;

/** Поля брифа в порядке вопросов диалога. */
public enum BriefField {
  TITLE("Как называется проект?", 3, 200),
  CUSTOMER("Какая организация заказывает проект?", 2, 200),
  DESCRIPTION("Что это за продукт и какую задачу он решает?", 30, 3000),
  EXPECTED_RESULT("Что должно получиться на выходе (MVP)?", 10, 2000),
  STACK(
      "Какой стек или ограничения нужно учесть? Если их нет, напишите «нет».",
      "Укажите стек или напишите «нет».",
      BriefField::allowsFreeText),
  DEADLINES(
      "Какие сроки или дедлайны нужно учесть?",
      "Укажите сроки в свободной форме.",
      BriefField::allowsFreeText),
  CUSTOMER_CONTACT(
      "Как связаться с заказчиком?",
      "Укажите почту, телефон или контакт в мессенджере.",
      BriefField::hasText);

  private final String question;
  private final String errorHint;
  private final Predicate<String> validator;

  BriefField(String question, int minLength, int maxLength) {
    this(
        question,
        "Введите от %d до %d символов.".formatted(minLength, maxLength),
        value -> {
          int length = value.codePointCount(0, value.length());
          return length >= minLength && length <= maxLength && !value.isBlank();
        });
  }

  BriefField(String question, String errorHint, Predicate<String> validator) {
    this.question = question;
    this.errorHint = errorHint;
    this.validator = validator;
  }

  public String question() {
    return question;
  }

  public String errorHint() {
    return errorHint;
  }

  public boolean isValid(String value) {
    return value != null && validator.test(value);
  }

  private static boolean allowsFreeText(String value) {
    return true;
  }

  private static boolean hasText(String value) {
    return !value.isBlank();
  }
}
