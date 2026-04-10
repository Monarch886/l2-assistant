package ru.che.lcp.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Гипотеза о причине инцидента и плане реагирования")
public class HypothesisDto {

    @Schema(
            description = "Предполагаемая причина инцидента",
            example = "Исчерпан пул соединений к базе данных из-за утечки соединений в сервисе авторизации"
    )
    private String possibleCause;

    @Schema(
            description = "Список рекомендуемых действий для устранения инцидента",
            example = "[\"Проверить метрики пула соединений\", \"Перезапустить сервис авторизации\"]"
    )
    private List<String> possibleActions;

    @Schema(
            description = "Вероятность гипотезы в процентах (0–100). Гипотезы упорядочены по убыванию.",
            example = "75"
    )
    private Integer confidence;
}