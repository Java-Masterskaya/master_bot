package ru.yandex.masterbot.domain.brief;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class BriefTest {
  @ParameterizedTest
  @CsvSource({"TITLE,3,200", "CUSTOMER,2,200", "DESCRIPTION,30,3000", "EXPECTED_RESULT,10,2000"})
  void validatesLengthInUnicodeCodePoints(BriefField field, int min, int max) {
    assertFalse(field.isValid("📋".repeat(min - 1)));
    assertTrue(field.isValid("📋".repeat(min)));
    assertTrue(field.isValid("📋".repeat(max)));
    assertFalse(field.isValid("📋".repeat(max + 1)));
    assertFalse(field.isValid(" ".repeat(min)));
  }

  @Test
  void validatesContactAndAllowsFreeText() {
    assertFalse(BriefField.CUSTOMER_CONTACT.isValid(" "));
    assertTrue(BriefField.CUSTOMER_CONTACT.isValid("user@example.org"));
    assertTrue(BriefField.STACK.isValid("нет"));
    assertTrue(BriefField.DEADLINES.isValid("После согласования MVP"));
  }

  @ParameterizedTest
  @EnumSource(BriefField.class)
  void rejectsMissingAnswer(BriefField field) {
    Map<BriefField, String> answers = validAnswers();
    answers.remove(field);

    IllegalArgumentException error =
        assertThrows(IllegalArgumentException.class, () -> new Brief(answers));

    assertTrue(error.getMessage().startsWith(field.name()));
  }

  @Test
  void keepsItsOwnImmutableCopyOfAnswers() {
    Map<BriefField, String> answers = validAnswers();
    Brief brief = new Brief(answers);
    answers.clear();

    assertEquals(validAnswers(), brief.answers());
    assertEquals(List.of(BriefField.values()), List.copyOf(brief.answers().keySet()));
    assertThrows(UnsupportedOperationException.class, brief.answers()::clear);
  }

  private static Map<BriefField, String> validAnswers() {
    return new EnumMap<>(
        Map.of(
            BriefField.TITLE, "Отчёты",
            BriefField.CUSTOMER, "Команда",
            BriefField.DESCRIPTION, "Собрать отчёты по проектам в одном месте.",
            BriefField.EXPECTED_RESULT, "Панель с отчётами",
            BriefField.STACK, "нет",
            BriefField.DEADLINES, "Один месяц",
            BriefField.CUSTOMER_CONTACT, "user@example.org"));
  }
}
