package ru.yandex.masterbot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

  // 1. Intake (лендинг) — только свой токен
  @Bean
  @Order(1)
  public SecurityFilterChain intakeFilterChain(
      HttpSecurity http,
      IntakeTokenFilter intakeTokenFilter,
      AuthenticationEntryPoint problemDetailEntryPoint)
      throws Exception {
    http.securityMatcher("/api/v1/intake/**")
        .authorizeHttpRequests(auth ->
            auth.requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**")
                .permitAll()
                .anyRequest()
                .authenticated())
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(intakeTokenFilter, UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(ex -> ex.authenticationEntryPoint(problemDetailEntryPoint));
    return http.build();
  }

  // 2. Admin (менеджеры) — только свой токен; deny-by-default
  @Bean
  @Order(2)
  public SecurityFilterChain adminFilterChain(
      HttpSecurity http,
      AdminTokenFilter adminTokenFilter,
      AuthenticationEntryPoint problemDetailEntryPoint)
      throws Exception {
    http.securityMatcher("/api/v1/**")
        .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(adminTokenFilter, UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(ex -> ex.authenticationEntryPoint(problemDetailEntryPoint));
    return http.build();
  }

  // 3. Всё остальное: открыто только health и swagger, остальное — deny
  @Bean
  @Order(3)
  public SecurityFilterChain defaultFilterChain(HttpSecurity http) throws Exception {
    http.securityMatcher("/**")
        .authorizeHttpRequests(auth ->
            auth.requestMatchers("/actuator/health/**", "/swagger-ui/**", "/v3/api-docs/**")
                .permitAll()
                .anyRequest()
                .denyAll())
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    return http.build();
  }

  // 401 в формате ProblemDetail
  @Bean
  public AuthenticationEntryPoint problemDetailEntryPoint(ObjectMapper objectMapper) {
    return (request, response, authException) -> {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

      ProblemDetail problemDetail =
          ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Authentication required");
      problemDetail.setTitle("Unauthorized");

      objectMapper.writeValue(response.getOutputStream(), problemDetail);
    };
  }
}
