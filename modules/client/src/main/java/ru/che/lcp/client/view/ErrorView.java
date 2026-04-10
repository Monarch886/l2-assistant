package ru.che.lcp.client.view;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@Schema(description = "Стандартный ответ об ошибке API")
public class ErrorView {

    @Schema(description = "HTTP статус код", example = "400")
    private int status;

    @Schema(
            description = "Машиночитаемый тип ошибки",
            example = "PROMPT_INJECTION",
            allowableValues = {"VALIDATION_ERROR", "PROMPT_INJECTION", "INTERNAL_ERROR"}
    )
    private String error;

    @Schema(description = "Человекочитаемое описание ошибки", example = "Prompt injection attempt detected: instruction override")
    private String message;

    @Schema(description = "Детали по конкретным полям — заполняется только при VALIDATION_ERROR",
            example = "{\"incidentDescription\": \"Описание инцидента не может быть пустым\"}")
    private Map<String, String> fieldErrors;
}