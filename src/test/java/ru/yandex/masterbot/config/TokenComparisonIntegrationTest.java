package ru.yandex.masterbot.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.masterbot.AbstractIntegrationTest;

@AutoConfigureMockMvc
@TestPropertySource(
    properties = {"app.admin.token=admin-secret-token", "app.intake.token=intake-secret-token"})
class TokenComparisonIntegrationTest extends AbstractIntegrationTest {

  private static final String ADMIN_TOKEN = "admin-secret-token";
  private static final String INTAKE_TOKEN = "intake-secret-token";

  @Autowired private MockMvc mockMvc;

  @Test
  void adminToken_onStats_passesSecurity() throws Exception {
    mockMvc
        .perform(get("/api/v1/stats").header("Authorization", "Bearer " + ADMIN_TOKEN))
        .andExpect(status().isNotImplemented()); // 501 — значит security пройден
  }

  @Test
  void intakeToken_onStats_returns401() throws Exception {
    mockMvc
        .perform(get("/api/v1/stats").header("Authorization", "Bearer " + INTAKE_TOKEN))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void adminToken_onIntake_returns401() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/intake/applications")
                .header("Authorization", "Bearer " + ADMIN_TOKEN)
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void intakeToken_onIntake_passesSecurity() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/intake/applications")
                .header("Authorization", "Bearer " + INTAKE_TOKEN)
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "title": "CRM",
                      "customer": "ООО Ромашка",
                      "description": "Нужно создать CRM для отдела продаж.",
                      "expectedResult": "Автоматизация",
                      "stack": "Java",
                      "deadlines": "3 месяца",
                      "customerContact": "manager@example.com"
                    }
                    """))
        .andExpect(status().isNotImplemented()); // 501 — security пройден
  }
}
