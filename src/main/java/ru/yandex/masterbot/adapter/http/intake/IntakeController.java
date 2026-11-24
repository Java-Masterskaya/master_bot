package ru.yandex.masterbot.adapter.http.intake;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.masterbot.adapter.http.intake.dto.IntakeApplicationRequest;
import ru.yandex.masterbot.adapter.http.intake.dto.IntakeApplicationResponse;

/** REST-контроллер публичного приёма заявок. */
@RestController
@RequestMapping("/api/v1/intake/applications")
@Tag(name = "Intake", description = "Публичный API приёма заявок")
public class IntakeController {

  @PostMapping
  @Operation(
      summary = "Принять новую заявку",
      description =
          "Принимает заявку из лендинг-формы. Для защиты от повторной отправки "
              + "требуется уникальный Idempotency-Key.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Заявка принята",
        content = @Content(schema = @Schema(implementation = IntakeApplicationResponse.class))),
    @ApiResponse(
        responseCode = "200",
        description = "Возвращена ранее созданная заявка при повторном запросе",
        content = @Content(schema = @Schema(implementation = IntakeApplicationResponse.class))),
    @ApiResponse(responseCode = "401", description = "Не передан или недействителен intake-токен"),
    @ApiResponse(
        responseCode = "422",
        description = "Некорректные данные или Idempotency-Key использован с другим телом запроса"),
    @ApiResponse(responseCode = "429", description = "Превышен лимит запросов"),
    @ApiResponse(responseCode = "501", description = "Реализация будет добавлена позднее")
  })
  public ResponseEntity<IntakeApplicationResponse> createApplication(
      @Parameter(
              description = "Ключ идемпотентности в формате UUID",
              required = true,
              example = "550e8400-e29b-41d4-a716-446655440000")
          @RequestHeader("Idempotency-Key")
          UUID idempotencyKey,
      @Valid @RequestBody IntakeApplicationRequest request) {

    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }
}
