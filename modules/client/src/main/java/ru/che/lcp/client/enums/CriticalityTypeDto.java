package ru.che.lcp.client.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Уровень критичности инцидента")
public enum CriticalityTypeDto {

    @Schema(description = "Низкая — влияние на бизнес минимально, есть обходное решение")
    LOW,

    @Schema(description = "Средняя — частичная деградация сервиса, обходное решение затруднено")
    MEDIUM,

    @Schema(description = "Высокая — значительная часть пользователей affected, бизнес-функции нарушены")
    HIGH,

    @Schema(description = "Критическая — полная недоступность сервиса или потеря данных")
    CRITICAL
}