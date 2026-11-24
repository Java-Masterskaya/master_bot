package ru.yandex.masterbot.adapter.http.intake;

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

/** HTTP-тесты публичного приёма заявок. */
@WebMvcTest(IntakeController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class IntakeControllerTest {

  private static final String IDEMPOTENCY_KEY = "550e8400-e29b-41d4-a716-446655440000";

  @Autowired private MockMvc mockMvc;

  @Test
  void createApplicationReturnsNotImplemented() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/intake/applications")
                .header("Idempotency-Key", IDEMPOTENCY_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "title": "CRM для отдела продаж",
                      "customer": "ООО Ромашка",
                      "description": "Нужно создать CRM для автоматизации работы отдела продаж.",
                      "expectedResult": "Автоматизация обработки заявок",
                      "stack": "Java, Spring Boot, PostgreSQL",
                      "deadlines": "3 месяца",
                      "customerContact": "manager@example.com"
                    }
                    """))
        .andExpect(status().isNotImplemented());
  }

  @Test
  void createApplicationWithShortDescriptionReturnsValidationProblem() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/intake/applications")
                .header("Idempotency-Key", IDEMPOTENCY_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "description": "Слишком коротко",
                      "customerContact": "manager@example.com"
                    }
                    """))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.title").value("Validation failed"))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.fieldErrors[0].field").value("description"));
  }

  @Test
  void createApplicationWithoutContactReturnsValidationProblem() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/intake/applications")
                .header("Idempotency-Key", IDEMPOTENCY_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "description": "Необходимо разработать CRM-систему для отдела продаж."
                    }
                    """))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.title").value("Validation failed"))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.fieldErrors[0].field").value("customerContact"));
  }
}
