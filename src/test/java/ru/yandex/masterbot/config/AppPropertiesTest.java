package ru.yandex.masterbot.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class AppPropertiesTest {
  private final ApplicationContextRunner runner =
      new ApplicationContextRunner()
          .withUserConfiguration(PropertiesConfiguration.class)
          .withPropertyValues(
              "app.db.url=jdbc:test",
              "app.db.username=user",
              "app.db.password=db-secret",
              "app.admin.token=admin-secret",
              "app.intake.token=intake-secret",
              "app.nats.url=nats://localhost:4222",
              "app.channels.telegram.enabled=false",
              "app.channels.yandex360.enabled=false",
              "app.managers-chat.channel=yandex360",
              "app.evaluation.enabled=false");

  @Test
  void enabledChannelsRequireTheirOwnToken() {
    for (String channel : new String[] {"telegram", "yandex360"}) {
      runner
          .withPropertyValues("app.channels." + channel + ".enabled=true")
          .run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure())
                .hasStackTraceContaining("app.channels." + channel + ".token is required");
          });
    }
  }

  @Test
  void configuredSecretsAreMasked() {
    runner
        .withPropertyValues(
            "app.channels.telegram.enabled=true",
            "app.channels.telegram.token=telegram-secret",
            "app.channels.telegram.mode=polling",
            "app.channels.yandex360.token=yandex-secret")
        .run(context -> {
          assertThat(context).hasNotFailed();
          String value = context.getBean(AppProperties.class).toString();
          assertThat(value)
              .doesNotContain(
                  "db-secret", "admin-secret", "intake-secret", "telegram-secret", "yandex-secret");
        });
  }

  @Configuration(proxyBeanMethods = false)
  @EnableConfigurationProperties(AppProperties.class)
  static class PropertiesConfiguration {}
}
