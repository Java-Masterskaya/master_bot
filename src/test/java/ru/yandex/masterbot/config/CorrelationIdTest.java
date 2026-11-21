package ru.yandex.masterbot.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdTest {
  @Test
  void httpIdIsReturnedAndContextIsRestoredAfterFailure() throws Exception {
    var response = new MockHttpServletResponse();
    MDC.put("existing", "value");
    try {
      try {
        new CorrelationIdFilter()
            .doFilter(
                new MockHttpServletRequest(),
                response,
                (req, res) -> {
                  assertThat(MDC.get(CorrelationId.MDC_KEY))
                      .isEqualTo(response.getHeader(CorrelationIdFilter.HEADER));
                  throw new ServletException("failure");
                });
      } catch (ServletException expected) {
        assertThat(expected).hasMessage("failure");
      }
      assertThat(response.getHeader(CorrelationIdFilter.HEADER)).isNotBlank();
      assertThat(MDC.get(CorrelationId.MDC_KEY)).isNull();
      assertThat(MDC.get("existing")).isEqualTo("value");
    } finally {
      MDC.clear();
    }
  }

  @Test
  void backgroundRunsGetDistinctIdsAndRestoreContext() throws IOException {
    String[] ids = new String[2];
    MDC.put("existing", "value");
    try {
      Runnable task =
          CorrelationId.decorate(() -> {
            if (ids[0] == null) {
              ids[0] = MDC.get(CorrelationId.MDC_KEY);
            } else {
              ids[1] = MDC.get(CorrelationId.MDC_KEY);
            }
          });
      task.run();
      task.run();
      assertThat(ids[0]).isNotBlank().isNotEqualTo(ids[1]);
      assertThat(MDC.get(CorrelationId.MDC_KEY)).isNull();
      assertThat(MDC.get("existing")).isEqualTo("value");
    } finally {
      MDC.clear();
    }
  }
}
