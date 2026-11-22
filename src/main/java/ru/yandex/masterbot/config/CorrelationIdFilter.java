package ru.yandex.masterbot.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Назначает каждому HTTP-запросу идентификатор для сопоставления логов. */
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {
  public static final String HEADER = "X-Correlation-ID";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    var previous = MDC.getCopyOfContextMap();
    String id = CorrelationId.generate();
    try {
      MDC.put(CorrelationId.MDC_KEY, id);
      response.setHeader(HEADER, id);
      filterChain.doFilter(request, response);
    } finally {
      MDC.clear();
      if (previous != null) {
        MDC.setContextMap(previous);
      }
    }
  }
}
