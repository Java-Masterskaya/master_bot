package ru.yandex.masterbot.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Все 16 пар статусов (4 × 4) покрыты одной параметризованной таблицей.
 *
 * <p>Формат строки CSV: source,target,expectedAllowed
 */
class ApplicationStatusTransitionTest {

  @ParameterizedTest(name = "{0} -> {1} : allowed={2}")
  @CsvSource({
    // source,     target,      expectedAllowed
    "NEW,         NEW,         false",
    "NEW,         IN_REVIEW,   true",
    "NEW,         ACCEPTED,    false",
    "NEW,         REJECTED,    false",
    "IN_REVIEW,   NEW,         false",
    "IN_REVIEW,   IN_REVIEW,   false",
    "IN_REVIEW,   ACCEPTED,    true",
    "IN_REVIEW,   REJECTED,    true",
    "ACCEPTED,    NEW,         false",
    "ACCEPTED,    IN_REVIEW,   false",
    "ACCEPTED,    ACCEPTED,    false",
    "ACCEPTED,    REJECTED,    false",
    "REJECTED,    NEW,         false",
    "REJECTED,    IN_REVIEW,   false",
    "REJECTED,    ACCEPTED,    false",
    "REJECTED,    REJECTED,    false"
  })
  @DisplayName("canTransitionTo: все 16 пар статусов")
  void canTransitionTo_allPairs(
      ApplicationStatus source, ApplicationStatus target, boolean expectedAllowed) {
    assertThat(source.canTransitionTo(target))
        .as("%s -> %s", source, target)
        .isEqualTo(expectedAllowed);
  }

  @ParameterizedTest
  @EnumSource(ApplicationStatus.class)
  @DisplayName("isTerminal: только ACCEPTED и REJECTED")
  void isTerminal_onlyAcceptedAndRejected(ApplicationStatus status) {
    boolean expected = status == ApplicationStatus.ACCEPTED || status == ApplicationStatus.REJECTED;
    assertThat(status.isTerminal()).isEqualTo(expected);
  }

  @Test
  @DisplayName("canTransitionTo(null) бросает NPE — правила переходов требуют явного target")
  void canTransitionTo_null_throws() {
    assertThatThrownBy(() -> ApplicationStatus.NEW.canTransitionTo(null))
        .isInstanceOf(NullPointerException.class);
  }
}
