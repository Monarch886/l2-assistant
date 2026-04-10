package ru.che.lcp.client;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.che.lcp.client.reqres.IncidentRequest;
import ru.che.lcp.client.view.ErrorView;
import ru.che.lcp.client.view.IncidentView;

/**
 * API-контракт микросервиса L2Agent для работы с инцидентами.
 *
 * <p>Этот интерфейс выполняет двойную роль:
 * <ul>
 *   <li><b>Серверная сторона</b> — реализуется Spring MVC контроллером в модуле {@code controller}.</li>
 *   <li><b>Клиентская сторона</b> — используется как Feign-клиент другими микросервисами,
 *       подключившими артефакт {@code ru.che.lcp:client} из Nexus.</li>
 * </ul>
 *
 * <p>Конфигурация Feign (URL, таймауты, перехватчики) задаётся на стороне потребителя
 * через application.yml: {@code l2agent.service.url}.
 */
@FeignClient(name = "l2agent", url = "${l2agent.service.url}")
@Tag(name = "Incidents", description = "API для приёма и обработки инцидентов")
@RequestMapping("/api/v1/incidents")
public interface IncidentController {

    @Operation(
            summary = "Зарегистрировать инцидент",
            description = """
                    Принимает raw текстовое описание инцидента в произвольной форме.
                    Описание передаётся в AI-pipeline для анализа, классификации и
                    формирования плана реагирования.
                    Возвращает структурированный результат анализа: категорию, критичность,
                    данные клиента и список гипотез с рекомендуемыми действиями.
                    """
    )
    @RequestBody(
            description = "Описание инцидента",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = IncidentRequest.class),
                    examples = @ExampleObject(
                            name = "Пример инцидента",
                            value = """
                                    {
                                      "incidentDescription": "Сервис авторизации недоступен с 14:30 MSK. \
                                    Пользователи не могут войти в систему. \
                                    В логах: Connection refused to auth-service:8080."
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Инцидент проанализирован, возвращён структурированный результат",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = IncidentView.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректный запрос: ошибка валидации (VALIDATION_ERROR) или обнаружен prompt injection (PROMPT_INJECTION)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorView.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Ошибка валидации",
                                            value = """
                                                    {
                                                      "status": 400,
                                                      "error": "VALIDATION_ERROR",
                                                      "fieldErrors": {
                                                        "incidentDescription": "Описание инцидента не может быть пустым"
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Prompt injection",
                                            value = """
                                                    {
                                                      "status": 400,
                                                      "error": "PROMPT_INJECTION",
                                                      "message": "Prompt injection attempt detected: instruction override"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorView.class)
                    )
            )
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<IncidentView> createIncident(
            @Valid @RequestBody IncidentRequest request
    );
}