package ru.yandex.masterbot.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.masterbot.adapter.http.admin.ApplicationController;
import ru.yandex.masterbot.adapter.http.admin.StatsController;
import ru.yandex.masterbot.adapter.http.intake.IntakeController;

@WebMvcTest(
    controllers = {ApplicationController.class, StatsController.class, IntakeController.class})
@Import(SecurityConfig.class)
@TestPropertySource(
    properties = {
      "app.admin.token=" + SecurityRoutingTest.ADMIN_TOKEN,
      "app.intake.token=" + SecurityRoutingTest.INTAKE_TOKEN
    })
class SecurityRoutingTest {

  static final String ADMIN_TOKEN = "admin-secret-token";
  static final String INTAKE_TOKEN = "intake-secret-token";

  @Autowired MockMvc mockMvc;

  /*
  Моки фильтров: контекст поднимается, но аутентификацию они не ставят.
  Значит любой запрос без явной аутентификации должен давать 401 на защищённых путях.
  */

  // ─────────────────────────────────────────────────────────────
  // 1. Без токена → 401 + ProblemDetail
  // ─────────────────────────────────────────────────────────────

  @Test
  @DisplayName("Admin: без токена → 401 ProblemDetail")
  void admin_noToken_returns401ProblemDetail() throws Exception {
    mockMvc
        .perform(get("/api/v1/stats"))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
  }

  @Test
  @DisplayName("Admin: без токена на /applications/{id} → 401")
  void admin_applications_noToken_returns401() throws Exception {
    mockMvc
        .perform(get("/api/v1/applications/{id}", UUID.randomUUID()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Intake: без токена → 401 ProblemDetail")
  void intake_noToken_returns401ProblemDetail() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/intake/applications")
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
  }

  // ─────────────────────────────────────────────────────────────
  // 2. Intake-токен на admin-путь → 401
  // ─────────────────────────────────────────────────────────────

  @Test
  @DisplayName("Intake-токен на /stats → 401")
  void intakeToken_onStats_returns401() throws Exception {
    mockMvc
        .perform(get("/api/v1/stats").header("Authorization", "Bearer " + INTAKE_TOKEN))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Intake-токен на /applications → 401")
  void intakeToken_onApplications_returns401() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/applications/{id}", UUID.randomUUID())
                .header("Authorization", "Bearer " + INTAKE_TOKEN))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Intake-токен на /applications/{id}/transitions → 401")
  void intakeToken_onTransitions_returns401() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/applications/{id}/transitions", UUID.randomUUID())
                .header("Authorization", "Bearer " + INTAKE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isUnauthorized());
  }

  // ─────────────────────────────────────────────────────────────
  // 3. Admin-токен на intake-путь → 401
  // ─────────────────────────────────────────────────────────────

  @Test
  @DisplayName("Admin-токен на /intake/applications → 401")
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

  // ─────────────────────────────────────────────────────────────
  // 4. Открытые пути (цепочка @Order(3))
  // ─────────────────────────────────────────────────────────────

  @Test
  @DisplayName("Swagger открыт без токена")
  void swagger_isPermitted() throws Exception {
    mockMvc.perform(get("/v3/api-docs")).andExpect(status().isNotFound()); // 404 от MVC, но НЕ 401
  }

  @Test
  @DisplayName("Actuator health открыт без токена")
  void actuatorHealth_isPermitted() throws Exception {
    mockMvc
        .perform(get("/actuator/health"))
        .andExpect(status().isNotFound()); // 404 от MVC, но НЕ 401
  }

  @Test
  @DisplayName("Произвольный путь вне /api/v1/** → denyAll → 403")
  void unknownPath_isDenied() throws Exception {
    mockMvc.perform(get("/some/unknown/path")).andExpect(status().isForbidden());
  }
}
