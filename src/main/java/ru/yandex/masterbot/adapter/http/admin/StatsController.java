package ru.yandex.masterbot.adapter.http.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.masterbot.adapter.http.admin.dto.StatsResponse;

/** HTTP-контроллер административной статистики. */
@RestController
@RequestMapping("/api/v1/stats")
@Tag(name = "Statistics", description = "Статистика по заявкам")
public class StatsController {

  @GetMapping
  @Operation(
      summary = "Получить статистику",
      description =
          "Возвращает агрегированную статистику по заявкам, "
              + "заявителям, рекомендациям и использованию токенов.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Статистика получена",
        content = @Content(schema = @Schema(implementation = StatsResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Отсутствует или неверный токен администратора",
        content = @Content),
    @ApiResponse(
        responseCode = "501",
        description = "Заглушка: реализация бизнес-логики ещё не выполнена",
        content = @Content)
  })
  public ResponseEntity<StatsResponse> getStats() {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }
}
