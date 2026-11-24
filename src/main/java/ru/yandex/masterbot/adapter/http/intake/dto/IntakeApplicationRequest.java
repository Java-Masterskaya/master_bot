package ru.yandex.masterbot.adapter.http.intake.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Данные заявки, поступившей через публичную intake-форму. */
@Schema(description = "Данные новой заявки из лендинг-формы")
public record IntakeApplicationRequest(
    @Size(min = 3, max = 200, message = "Название должно содержать от 3 до 200 символов")
        @Schema(description = "Название проекта", example = "CRM для отдела продаж")
        String title,
    @Size(min = 2, max = 200, message = "Название заказчика должно содержать от 2 до 200 символов")
        @Schema(description = "Организация-заказчик", example = "ООО Ромашка")
        String customer,
    @NotBlank(message = "Описание обязательно")
        @Size(min = 30, max = 3000, message = "Описание должно содержать от 30 до 3000 символов")
        @Schema(
            description = "Описание проекта, не менее 30 символов",
            example = "Необходимо разработать CRM-систему для автоматизации работы отдела продаж.",
            requiredMode = Schema.RequiredMode.REQUIRED)
        String description,
    @Size(
            min = 10,
            max = 2000,
            message = "Ожидаемый результат должен содержать от 10 до 2000 символов")
        @Schema(
            description = "Ожидаемый результат проекта",
            example = "Автоматизация обработки заявок")
        String expectedResult,
    @Schema(description = "Технологический стек", example = "Java, Spring Boot, PostgreSQL")
        String stack,
    @Schema(description = "Желаемые сроки реализации", example = "3 месяца") String deadlines,
    @NotBlank(message = "Контакт заказчика обязателен")
        @Schema(
            description = "Контакт для обратной связи",
            example = "manager@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
        String customerContact) {}
