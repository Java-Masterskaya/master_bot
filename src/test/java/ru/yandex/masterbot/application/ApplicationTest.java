package ru.yandex.masterbot.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.masterbot.domain.Application;
import ru.yandex.masterbot.domain.ApplicationStatus;
import ru.yandex.masterbot.domain.IllegalStatusTransitionException;
import ru.yandex.masterbot.domain.StatusChange;

class ApplicationTest {

  private static final Instant NOW = Instant.parse("2024-01-01T00:00:00Z");
  private static final UUID APP_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

  private Application newApplication() {
    return Application.create(APP_ID, 1001L, "TELEMOST", NOW);
  }

  @Test
  @DisplayName("Новая заявка имеет статус NEW, версию 0 и пустую историю")
  void createdWithDefaults() {
    Application app = newApplication();

    assertThat(app.status()).isEqualTo(ApplicationStatus.NEW);
    assertThat(app.version()).isZero();
    assertThat(app.history()).isEmpty();
  }

  @Test
  @DisplayName("NEW -> IN_REVIEW: создаёт запись истории и увеличивает версию")
  void transition_newToInReview() {
    Application app = newApplication();

    Application updated =
        app.changeStatus(
            ApplicationStatus.IN_REVIEW, "admin-1", "Взят в работу", NOW.plusSeconds(60));

    assertThat(updated.status()).isEqualTo(ApplicationStatus.IN_REVIEW);
    assertThat(updated.version()).isEqualTo(1L);
    assertThat(updated.history()).hasSize(1);

    StatusChange change = updated.history().getFirst();
    assertThat(change.from()).isEqualTo(ApplicationStatus.NEW);
    assertThat(change.to()).isEqualTo(ApplicationStatus.IN_REVIEW);
    assertThat(change.actor()).isEqualTo("admin-1");
    assertThat(change.comment()).isEqualTo("Взят в работу");
    assertThat(change.at()).isEqualTo(NOW.plusSeconds(60));
  }

  @Test
  @DisplayName("IN_REVIEW -> ACCEPTED и IN_REVIEW -> REJECTED проходят успешно")
  void transition_inReviewToTerminal() {
    Application inReview =
        newApplication().changeStatus(ApplicationStatus.IN_REVIEW, "admin-1", null, NOW);

    Application accepted =
        inReview.changeStatus(ApplicationStatus.ACCEPTED, "admin-1", "Ок", NOW.plusSeconds(10));
    assertThat(accepted.status()).isEqualTo(ApplicationStatus.ACCEPTED);
    assertThat(accepted.history()).hasSize(2);

    Application rejected =
        inReview.changeStatus(ApplicationStatus.REJECTED, "admin-1", "Нет", NOW.plusSeconds(10));
    assertThat(rejected.status()).isEqualTo(ApplicationStatus.REJECTED);
    assertThat(rejected.history()).hasSize(2);
  }

  @Test
  @DisplayName("Запрещённый переход бросает IllegalStatusTransitionException")
  void forbiddenTransition_throws() {
    Application app = newApplication();

    assertThatThrownBy(() -> app.changeStatus(ApplicationStatus.ACCEPTED, "admin-1", null, NOW))
        .isInstanceOf(IllegalStatusTransitionException.class)
        .hasMessageContaining("NEW")
        .hasMessageContaining("ACCEPTED");
  }

  @Test
  @DisplayName("Терминальный статус не допускает дальнейших переходов")
  void terminalStatus_hasNoTransitions() {
    Application accepted =
        newApplication()
            .changeStatus(ApplicationStatus.IN_REVIEW, "admin-1", null, NOW)
            .changeStatus(ApplicationStatus.ACCEPTED, "admin-1", null, NOW.plusSeconds(10));

    assertThatThrownBy(() ->
            accepted.changeStatus(ApplicationStatus.REJECTED, "admin-1", null, NOW.plusSeconds(20)))
        .isInstanceOf(IllegalStatusTransitionException.class);
  }

  @Test
  @DisplayName("changeStatus на тот же статус запрещён")
  void sameStatus_throws() {
    Application app = newApplication();

    assertThatThrownBy(() -> app.changeStatus(ApplicationStatus.NEW, "admin-1", null, NOW))
        .isInstanceOf(IllegalStatusTransitionException.class);
  }

  @Test
  @DisplayName("history иммутабельна: список только для чтения")
  void history_isImmutable() {
    Application app =
        newApplication().changeStatus(ApplicationStatus.IN_REVIEW, "admin-1", null, NOW);

    assertThatThrownBy(() ->
            app.history()
                .add(
                    new StatusChange(
                        ApplicationStatus.NEW, ApplicationStatus.ACCEPTED, "x", null, NOW)))
        .isInstanceOf(UnsupportedOperationException.class);
  }
}
