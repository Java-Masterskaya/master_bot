package ru.yandex.masterbot.adapter.yandex360.exception;

public class YandexApiException extends RuntimeException {

  private final Integer statusCode;

  public YandexApiException(Integer statusCode, String description) {
    super(description);
    this.statusCode = statusCode;
  }

  public Integer getStatusCode() {
    return statusCode;
  }
}
