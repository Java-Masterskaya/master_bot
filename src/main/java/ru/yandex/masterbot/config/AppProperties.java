package ru.yandex.masterbot.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Типизированные настройки сервиса. */
@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
    @Valid @NotNull Db db,
    @Valid @NotNull Token admin,
    @Valid @NotNull Token intake,
    @Valid @NotNull Nats nats,
    @Valid @NotNull Channels channels,
    @Valid @NotNull ManagersChat managersChat,
    @Valid @NotNull Evaluation evaluation) {

  public record Db(String url, String username, String password) {
    @Override
    public String toString() {
      return "Db[url=" + url + ", username=" + username + ", password=***]";
    }
  }

  public record Token(String token) {
    @Override
    public String toString() {
      return "Token[token=***]";
    }
  }

  public record Nats(String url) {}

  public record Channels(@Valid @NotNull Channel telegram, @Valid @NotNull Channel yandex360) {
    @AssertTrue(message = "app.channels.telegram.token is required when telegram is enabled")
    public boolean isTelegramTokenConfigured() {
      return telegram == null || !telegram.enabled() || hasText(telegram.token());
    }

    @AssertTrue(message = "app.channels.yandex360.token is required when yandex360 is enabled")
    public boolean isYandex360TokenConfigured() {
      return yandex360 == null || !yandex360.enabled() || hasText(yandex360.token());
    }

    private static boolean hasText(String value) {
      return value != null && !value.isBlank();
    }
  }

  public record Channel(boolean enabled, String token, Mode mode) {
    @Override
    public String toString() {
      return "Channel[enabled=" + enabled + ", token=***, mode=" + mode + "]";
    }
  }

  public enum Mode {
    POLLING,
    WEBHOOK
  }

  public record ManagersChat(String channel, String chatId) {}

  public record Evaluation(boolean enabled) {}
}
