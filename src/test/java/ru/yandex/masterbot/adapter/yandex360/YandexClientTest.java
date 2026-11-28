package ru.yandex.masterbot.adapter.yandex360;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.net.http.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import ru.yandex.masterbot.adapter.yandex360.client.YandexClient;
import ru.yandex.masterbot.adapter.yandex360.dto.SendTextRequest;
import ru.yandex.masterbot.adapter.yandex360.dto.SendTextResponse;
import ru.yandex.masterbot.adapter.yandex360.dto.UpdatesResponse;
import ru.yandex.masterbot.adapter.yandex360.exception.YandexApiException;

class YandexClientTest {

  private WireMockServer wireMockServer;

  private YandexClient yandexClient;

  private static final String TOKEN = "test-token";

  @BeforeEach
  void setUp() {
    wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());

    wireMockServer.start();

    HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

    RestClient restClient =
        RestClient.builder()
            .requestFactory(new JdkClientHttpRequestFactory(httpClient))
            .baseUrl(wireMockServer.baseUrl() + "/bot/v1")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "OAuth " + TOKEN)
            .build();

    yandexClient = new YandexClient(restClient, new ObjectMapper());
  }

  @AfterEach
  void tearDown() {
    wireMockServer.stop();
  }

  @Test
  void shouldGetUpdates() {
    wireMockServer.stubFor(
        get(urlPathEqualTo("/bot/v1/messages/getUpdates/"))
            .withQueryParam("offset", equalTo("100"))
            .withQueryParam("limit", equalTo("10"))
            .withHeader("Authorization", equalTo("OAuth " + TOKEN))
            .willReturn(
                okJson(
                    """
                    {
                      "ok": true,
                      "updates": [
                        {
                          "update_id": 101,
                          "message_id": 123456,
                          "timestamp": 1700000000,
                          "chat": {
                            "type": "private",
                            "id": "chat-123"
                          },
                          "from": {
                            "id": "user-123",
                            "display_name": "Test User",
                            "login": "test",
                            "robot": false
                          },
                          "text": "Hello"
                        }
                      ]
                    }
                    """)));

    UpdatesResponse response = yandexClient.getUpdates(100, 10);

    assertTrue(response.ok());
    assertEquals(1, response.updates().size());
    assertEquals(101L, response.updates().getFirst().updateId());
    assertEquals("Hello", response.updates().getFirst().text());

    wireMockServer.verify(getRequestedFor(urlPathEqualTo("/bot/v1/messages/getUpdates/")));
  }

  @Test
  void shouldSendText() {
    wireMockServer.stubFor(
        post(urlPathEqualTo("/bot/v1/messages/sendText/"))
            .withHeader("Authorization", equalTo("OAuth " + TOKEN))
            .withHeader("Content-Type", containing("application/json"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        """
                        {
                          "ok": true,
                          "message_id": 123456
                        }
                        """)));

    try {
      SendTextResponse response = yandexClient.sendText(new SendTextRequest("chat-123", "Hello!"));

      assertTrue(response.ok());
      assertEquals(123456L, response.messageId());
    } catch (Exception e) {
      wireMockServer
          .getAllServeEvents()
          .forEach(event ->
              System.out.println(
                  event.getRequest().getMethod()
                      + " "
                      + event.getRequest().getUrl()
                      + "\n"
                      + event.getRequest().getBodyAsString()));

      throw e;
    }
  }

  @Test
  void shouldThrowExceptionOnHttpError() {
    wireMockServer.stubFor(
        get(urlPathEqualTo("/bot/v1/messages/getUpdates/"))
            .willReturn(
                aResponse()
                    .withStatus(401)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        """
                        {
                          "ok": false,
                          "description": "Unauthorized"
                        }
                        """)));

    YandexApiException exception =
        assertThrows(YandexApiException.class, () -> yandexClient.getUpdates(0, 10));

    assertEquals(401, exception.getStatusCode());
    assertEquals("Unauthorized", exception.getMessage());
  }

  @Test
  void shouldThrowExceptionWhenApiReturnsOkFalse() {
    wireMockServer.stubFor(
        post(urlPathEqualTo("/bot/v1/messages/sendText/"))
            .willReturn(
                okJson(
                    """
                    {
                      "ok": false,
                      "description": "Bot is not a member of the chat"
                    }
                    """)));

    YandexApiException exception =
        assertThrows(
            YandexApiException.class,
            () -> yandexClient.sendText(new SendTextRequest("chat-123", "Hello!")));

    assertNull(exception.getStatusCode());
    assertEquals("Bot is not a member of the chat", exception.getMessage());
  }
}
