package ru.yandex.masterbot.adapter.yandex360.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.yandex.masterbot.adapter.yandex360.dto.SendTextRequest;
import ru.yandex.masterbot.adapter.yandex360.dto.SendTextResponse;
import ru.yandex.masterbot.adapter.yandex360.dto.UpdatesResponse;
import ru.yandex.masterbot.adapter.yandex360.exception.YandexApiException;
import ru.yandex.masterbot.adapter.yandex360.exception.YandexErrorResponse;

@Component
public class YandexClient {

  private final RestClient client;
  private final ObjectMapper objectMapper;

  public YandexClient(@Qualifier("yandexRestClient") RestClient client, ObjectMapper objectMapper) {
    this.client = client;
    this.objectMapper = objectMapper;
  }

  /**
   * Получает обновления (сообщения и другие события) от API Яндекс Мессенджера.
   *
   * @param offset идентификатор обновления, начиная с которого нужно получить данные
   * @param limit максимальное количество обновлений (от 1 до 1000)
   * @return ответ API со списком обновлений
   * @throws IllegalArgumentException если параметры запроса имеют недопустимые значения
   * @throws YandexApiException если API вернул HTTP-ошибку или признак ok=false
   */
  public UpdatesResponse getUpdates(long offset, int limit) {

    // Проверяем параметры, чтобы не отправлять заведомо некорректный запрос в API.
    if (offset < 0 || limit < 1 || limit > 1000) {
      throw new IllegalArgumentException("Invalid offset or limit");
    }

    // Выполняем GET-запрос и передаём параметры offset и limit через query string.
    UpdatesResponse response =
        client
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/messages/getUpdates/")
                    .queryParam("offset", offset)
                    .queryParam("limit", limit)
                    .build())
            .retrieve()

            // Перехватываем HTTP-ошибки (например, 401 или 500)
            // и преобразуем их в собственное исключение YandexApiException.
            .onStatus(HttpStatusCode::isError, this::handleError)

            // Десериализуем JSON-ответ API в DTO UpdatesResponse.
            .body(UpdatesResponse.class);

    // Проверяем не только HTTP-статус, но и результат выполнения операции на стороне API.
    return checkResponse(response);
  }

  /**
   * Отправляет текстовое сообщение в указанный чат.
   *
   * @param request данные сообщения: идентификатор чата и текст
   * @return ответ API с результатом отправки и идентификатором сообщения
   * @throws YandexApiException если API вернул HTTP-ошибку или признак ok=false
   */
  public SendTextResponse sendText(SendTextRequest request) {

    // Выполняем POST-запрос, передавая данные сообщения в формате JSON.
    SendTextResponse response =
        client
            .post()
            .uri("/messages/sendText/")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()

            // Обрабатываем HTTP-ошибки единым способом для всех методов клиента.
            .onStatus(HttpStatusCode::isError, this::handleError)

            // Преобразуем JSON-ответ в DTO SendTextResponse.
            .body(SendTextResponse.class);

    // Проверяем поле ok: API может вернуть HTTP 200 даже при неуспешной операции.
    return checkResponse(response);
  }

  /**
   * Обрабатывает HTTP-ошибки, возвращённые API.
   *
   * <p>Извлекает описание ошибки из JSON-поля description. Если тело ответа не соответствует
   * ожидаемому формату, использует текст ответа или стандартное описание HTTP-статуса.
   *
   * @param request исходный HTTP-запрос
   * @param response HTTP-ответ с ошибочным статусом
   * @throws IOException если не удалось прочитать тело HTTP-ответа
   * @throws YandexApiException исключение с HTTP-статусом и описанием ошибки
   */
  private void handleError(HttpRequest request, @NonNull ClientHttpResponse response)
      throws IOException {

    // Читаем тело ответа, чтобы получить подробное описание ошибки от API.
    String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);

    String description;

    try {
      // Пытаемся извлечь описание ошибки из стандартного JSON-ответа Яндекс API.
      description = objectMapper.readValue(body, YandexErrorResponse.class).description();
    } catch (Exception e) {
      // Если JSON отсутствует или имеет неожиданный формат,
      // используем текст ответа либо стандартное описание HTTP-статуса.
      description = body.isBlank() ? response.getStatusText() : body;
    }

    // Прерываем выполнение и передаём вызывающему коду понятную информацию об ошибке.
    throw new YandexApiException(response.getStatusCode().value(), description);
  }

  /**
   * Проверяет успешность операции на уровне самого API.
   *
   * <p>HTTP-статус может быть 200 OK, но API при этом может вернуть ok=false. В таком случае
   * выбрасываем YandexApiException без HTTP-статуса.
   *
   * @param response десериализованный ответ API
   * @return исходный ответ, если операция выполнена успешно
   */
  private <T> T checkResponse(T response) {

    // Проверяем результат получения обновлений.
    if (response instanceof UpdatesResponse updates && !updates.ok()) {
      throw new YandexApiException(null, updates.description());
    }

    // Проверяем результат отправки сообщения.
    if (response instanceof SendTextResponse send && !send.ok()) {
      throw new YandexApiException(null, send.description());
    }

    // Если ошибок нет, возвращаем исходный ответ без изменений.
    return response;
  }
}
