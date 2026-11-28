package ru.yandex.masterbot.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import ru.yandex.masterbot.adapter.yandex360.properties.YandexProperties;

@Configuration
@EnableConfigurationProperties(YandexProperties.class)
public class RestClientConfiguration {

  @Bean
  @Qualifier("yandexRestClient")
  public RestClient yandexRestClient(RestClient.Builder builder, YandexProperties properties) {
    return builder
        .clone()
        .baseUrl(properties.baseUrl())
        .defaultHeader(HttpHeaders.AUTHORIZATION, "OAuth " + properties.token())
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }
}
