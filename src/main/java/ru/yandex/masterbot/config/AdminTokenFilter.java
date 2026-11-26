package ru.yandex.masterbot.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AdminTokenFilter extends OncePerRequestFilter {

  private final byte[] adminToken;

  public AdminTokenFilter(@Value("${app.admin.token}") String rawToken) {
    if (rawToken == null || rawToken.isBlank()) {
      throw new IllegalStateException("app.admin.token is not configured");
    }
    this.adminToken = rawToken.getBytes(StandardCharsets.UTF_8);
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      byte[] providedToken = authHeader.substring(7).getBytes(StandardCharsets.UTF_8);

      // Постоянное время сравнения
      if (MessageDigest.isEqual(adminToken, providedToken)) {
        // Успех: устанавливаем аутентификацию
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(
                "admin",
                null, // credentials = null, это нормально для stateless Bearer-аутентификации
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);
      }
    }

    filterChain.doFilter(request, response);
  }
}
