package ru.yandex.masterbot.adapter.http.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.masterbot.adapter.http.admin.dto.ApplicationDetailsResponse;
import ru.yandex.masterbot.adapter.http.admin.dto.ApplicationListResponse;
import ru.yandex.masterbot.adapter.http.admin.dto.TransitionRequest;

/** HTTP-контроллер административных операций с заявками. */
@RestController
@RequestMapping("/api/v1/applications")
@Tag(name = "Applications", description = "Управление заявками")
public class ApplicationController {

  @GetMapping("/{id}")
  @Operation(
      summary = "Получить карточку заявки",
      description =
          "Возвращает полную информацию о заявке, включая поля брифа, "
              + "заявителя, историю статусов и результат оценки.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Карточка заявки получена",
        content = @Content(schema = @Schema(implementation = ApplicationDetailsResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Отсутствует или неверный токен администратора",
        content = @Content),
    @ApiResponse(responseCode = "404", description = "Заявка не найдена", content = @Content),
    @ApiResponse(
        responseCode = "501",
        description = "Заглушка: реализация бизнес-логики ещё не выполнена",
        content = @Content)
  })
  public ResponseEntity<ApplicationDetailsResponse> getApplication(
      @Parameter(description = "UUID заявки", in = ParameterIn.PATH) @PathVariable UUID id) {

    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  @GetMapping
  @Operation(
      summary = "Получить список заявок",
      description =
          "Возвращает список заявок с фильтрацией и курсорной пагинацией. "
              + "Курсор используется для перехода к следующей странице. "
              + "Состав полей элемента списка и структура ответа являются предварительными "
              + "и подлежат согласованию с командой.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Список заявок получен",
        content = @Content(schema = @Schema(implementation = ApplicationListResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Отсутствует или неверный токен администратора",
        content = @Content),
    @ApiResponse(
        responseCode = "501",
        description = "Заглушка: реализация бизнес-логики ещё не выполнена",
        content = @Content)
  })
  public ResponseEntity<ApplicationListResponse> getApplications(
      @Parameter(description = "Фильтр по статусу заявки", example = "IN_REVIEW")
          @RequestParam(required = false)
          String status,
      @Parameter(description = "Фильтр по источнику заявки", example = "LANDING")
          @RequestParam(required = false)
          String source,
      @Parameter(description = "Фильтр по рекомендации", example = "ACCEPT")
          @RequestParam(required = false)
          String recommendation,
      @Parameter(
              description = "Начало периода создания заявки включительно",
              example = "2026-11-01T00:00:00Z")
          @RequestParam(required = false)
          Instant createdFrom,
      @Parameter(
              description = "Конец периода создания заявки включительно",
              example = "2026-11-23T23:59:59Z")
          @RequestParam(required = false)
          Instant createdTo,
      @Parameter(description = "Логин заявителя", example = "ivanov")
          @RequestParam(required = false)
          String requester,
      @Parameter(description = "Поиск по подстроке в названии заявки", example = "CRM")
          @RequestParam(required = false)
          String q,
      @Parameter(description = "Курсор следующей страницы") @RequestParam(required = false)
          String cursor,
      @Parameter(
              description = "Количество заявок на странице, от 1 до 100",
              schema = @Schema(type = "integer", minimum = "1", maximum = "100"))
          @RequestParam(required = false)
          Integer limit) {

    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  @PostMapping("/{id}/transitions")
  @Operation(
      summary = "Изменить статус заявки",
      description = "Принимает целевой статус и комментарий. Актор передаётся в заголовке X-Actor.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Статус изменён или уже установлен"),
    @ApiResponse(
        responseCode = "401",
        description = "Отсутствует или неверный токен администратора",
        content = @Content),
    @ApiResponse(responseCode = "404", description = "Заявка не найдена", content = @Content),
    @ApiResponse(
        responseCode = "409",
        description = "Конфликт версий при конкурентном изменении",
        content = @Content),
    @ApiResponse(
        responseCode = "422",
        description = "Недопустимый переход или ошибки валидации",
        content = @Content),
    @ApiResponse(
        responseCode = "501",
        description = "Заглушка: реализация бизнес-логики ещё не выполнена",
        content = @Content)
  })
  public ResponseEntity<Void> transition(
      @Parameter(description = "UUID заявки", in = ParameterIn.PATH) @PathVariable UUID id,
      @Parameter(description = "Актор, инициирующий переход", in = ParameterIn.HEADER)
          @RequestHeader(value = "X-Actor", required = false)
          String actor,
      @Valid @RequestBody TransitionRequest request) {

    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  @PostMapping("/{id}/evaluations")
  @Operation(
      summary = "Перезапустить оценку заявки",
      description =
          "Создаёт новое задание оценки заявки. "
              + "Если уже выполняется активное задание, возвращается конфликт.")
  @ApiResponses({
    @ApiResponse(responseCode = "202", description = "Задание оценки принято"),
    @ApiResponse(
        responseCode = "401",
        description = "Отсутствует или неверный токен администратора",
        content = @Content),
    @ApiResponse(responseCode = "404", description = "Заявка не найдена", content = @Content),
    @ApiResponse(
        responseCode = "409",
        description = "Для заявки уже выполняется оценка",
        content = @Content),
    @ApiResponse(
        responseCode = "501",
        description = "Заглушка: реализация бизнес-логики ещё не выполнена",
        content = @Content)
  })
  public ResponseEntity<Void> restartEvaluation(
      @Parameter(description = "UUID заявки", in = ParameterIn.PATH) @PathVariable UUID id) {

    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }
}
