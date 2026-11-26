package ru.yandex.masterbot.adapter.http.admin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.masterbot.config.SecurityConfig;

/** HTTP-тесты контроллера административной статистики. */
@WebMvcTest(StatsController.class)
@Import(SecurityConfig.class)
@TestPropertySource(
    properties = {"app.admin.token=admin-secret-token", "app.intake.token=intake-secret-token"})
class StatsControllerTest {

  private static final String ADMIN_TOKEN = "admin-secret-token";

  @Autowired private MockMvc mockMvc;

  @Test
  void getStatsReturnsNotImplemented() throws Exception {
    mockMvc
        .perform(get("/api/v1/stats").header("Authorization", "Bearer " + ADMIN_TOKEN))
        .andExpect(status().isNotImplemented());
  }

  @Test
  void getStatsWithoutTokenReturns401() throws Exception {
    mockMvc
        .perform(get("/api/v1/stats"))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
  }

  @Test
  void getStatsWithIntakeTokenReturns401() throws Exception {
    mockMvc
        .perform(get("/api/v1/stats").header("Authorization", "Bearer intake-secret-token"))
        .andExpect(status().isUnauthorized());
  }
}
