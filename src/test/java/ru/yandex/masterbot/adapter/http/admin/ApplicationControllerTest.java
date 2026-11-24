package ru.yandex.masterbot.adapter.http.admin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.masterbot.adapter.http.error.GlobalExceptionHandler;
import ru.yandex.masterbot.config.SecurityConfig;

/** HTTP-тесты административного контроллера заявок. */
@WebMvcTest(ApplicationController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ApplicationControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void getApplicationsReturnsNotImplemented() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/applications")
                .queryParam("status", "IN_REVIEW")
                .queryParam("source", "LANDING")
                .queryParam("recommendation", "ACCEPT")
                .queryParam("createdFrom", "2026-11-01T00:00:00Z")
                .queryParam("createdTo", "2026-11-23T23:59:59Z")
                .queryParam("requester", "ivanov")
                .queryParam("q", "CRM")
                .queryParam("cursor", "next-page-cursor")
                .queryParam("limit", "50"))
        .andExpect(status().isNotImplemented());
  }

  @Test
  void transitionReturnsNotImplemented() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/applications/550e8400-e29b-41d4-a716-446655440000/transitions")
                .header("X-Actor", "manager")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "status": "IN_REVIEW",
                      "comment": "Начали проверку заявки"
                    }
                    """))
        .andExpect(status().isNotImplemented());
  }

  @Test
  void transitionWithoutStatusReturnsValidationProblem() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/applications/550e8400-e29b-41d4-a716-446655440000/transitions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "comment": "Начали проверку заявки"
                    }
                    """))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.title").value("Validation failed"))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.fieldErrors[0].field").value("status"))
        .andExpect(jsonPath("$.fieldErrors[0].message").value("Статус обязателен"));
  }

  @Test
  void restartEvaluationReturnsNotImplemented() throws Exception {
    mockMvc
        .perform(post("/api/v1/applications/550e8400-e29b-41d4-a716-446655440000/evaluations"))
        .andExpect(status().isNotImplemented());
  }

  @Test
  void getApplicationReturnsNotImplemented() throws Exception {
    mockMvc
        .perform(get("/api/v1/applications/550e8400-e29b-41d4-a716-446655440000"))
        .andExpect(status().isNotImplemented());
  }
}
