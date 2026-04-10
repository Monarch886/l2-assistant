package ru.che.lcp.client.exception;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Бросается при обнаружении попытки prompt injection в описании инцидента.
 * Обрабатывается {@code ExceptionHandlerController} → HTTP 400.
 */
@Schema(description = "Попытка prompt injection в тексте описания инцидента")
public class PromptInjectionException extends RuntimeException {

    @Schema(description = "Тип обнаруженной атаки", example = "instruction override")
    private final String detectedPattern;

    public PromptInjectionException(String detectedPattern) {
        super("Prompt injection attempt detected: " + detectedPattern);
        this.detectedPattern = detectedPattern;
    }

    public String getDetectedPattern() {
        return detectedPattern;
    }
}