package ru.che.lcp.client.view;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.che.lcp.client.dto.UserDto;
import ru.che.lcp.client.enums.CriticalityTypeDto;
import ru.che.lcp.client.dto.HypothesisDto;

import java.util.List;

@Data
@Schema(description = "Структурированный результат AI-анализа инцидента")
public class IncidentView {

    @Schema(
            description = "Категория инцидента, определённая по результатам анализа",
            example = "INFRASTRUCTURE"
    )
    private String category;

    @Schema(
            description = "Развёрнутое описание инцидента на основе анализа входных данных",
            example = "Сервис авторизации недоступен вследствие исчерпания пула соединений к PostgreSQL"
    )
    private String description;

    @Schema(description = "Уровень критичности инцидента")
    private CriticalityTypeDto criticality;

    @Schema(description = "Данные клиента, инициировавшего или связанного с инцидентом")
    private UserDto user;

    @Schema(description = "Список гипотез о причинах инцидента и рекомендуемых действиях, упорядоченных по вероятности")
    private List<HypothesisDto> hypotheses;
}