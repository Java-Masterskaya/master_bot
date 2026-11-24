package ru.yandex.masterbot.adapter.http.error;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Общая обработка ошибок HTTP API. */
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleValidationException(
      MethodArgumentNotValidException exception) {

    List<Map<String, String>> fieldErrors =
        exception.getBindingResult().getFieldErrors().stream()
            .map(error ->
                Map.of(
                    "field",
                    error.getField(),
                    "message",
                    error.getDefaultMessage() == null
                        ? "Некорректное значение"
                        : error.getDefaultMessage()))
            .toList();

    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.UNPROCESSABLE_ENTITY, "Переданные данные не прошли проверку");
    problem.setTitle("Validation failed");
    problem.setProperty("fieldErrors", fieldErrors);

    return ResponseEntity.unprocessableEntity().body(problem);
  }
}
