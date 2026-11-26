package ru.yandex.masterbot.adapter.http.admin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.masterbot.config.SecurityConfig;

/** HTTP-тесты контроллера административной статистики. */
@WebMvcTest(StatsController.class)
@Import(SecurityConfig.class)
class StatsControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void getStatsReturnsNotImplemented() throws Exception {
    mockMvc.perform(get("/api/v1/stats")).andExpect(status().isNotImplemented());
  }
}
