package ru.che.lcp.client.reqres;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Запрос на создание инцидента")
public class IncidentRequest {

    @NotBlank(message = "Описание инцидента не может быть пустым")
    @Size(min = 10, max = 5000, message = "Описание должно содержать от 10 до 5000 символов")
    @Schema(
            description = "Raw текстовое описание инцидента в произвольной форме",
            example = "Сервис авторизации недоступен с 14:30 MSK. Пользователи не могут войти в систему. " +
                      "В логах: Connection refused to auth-service:8080.",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 10,
            maxLength = 5000
    )
    private String incidentDescription;
}