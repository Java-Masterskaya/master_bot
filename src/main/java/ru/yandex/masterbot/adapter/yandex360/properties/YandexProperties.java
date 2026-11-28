package ru.yandex.masterbot.adapter.yandex360.properties;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.channels.yandex")
public record YandexProperties(String baseUrl, String token) {
  @Override
  public @NonNull String toString() {
    return "YandexProperties[baseUrl=" + baseUrl + ", token=***]";
  }
}
