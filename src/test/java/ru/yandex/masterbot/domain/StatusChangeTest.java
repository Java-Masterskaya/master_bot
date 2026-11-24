package ru.yandex.masterbot.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class StatusChangeTest {

  private static final Instant NOW = Instant.parse("2024-01-01T00:00:00Z");

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "\t"})
  @DisplayName("actor обязателен для любой смены статуса")
  void actor_isRequired(String actor) {
    assertThatThrownBy(() ->
            new StatusChange(ApplicationStatus.NEW, ApplicationStatus.IN_REVIEW, actor, null, NOW))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("actor");
  }

  @Test
  @DisplayName("Валидный StatusChange создаётся без исключений")
  void validChange_isCreated() {
    assertThatCode(() ->
            new StatusChange(
                ApplicationStatus.NEW, ApplicationStatus.IN_REVIEW, "admin-1", "ok", NOW))
        .doesNotThrowAnyException();
  }
}
