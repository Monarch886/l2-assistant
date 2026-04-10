package ru.che.lcp.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.che.lcp.domain.enums.CriticalityType;

import java.util.List;

/**
 * Доменная модель результата AI-анализа инцидента.
 * Вложенные классы специально снабжены @NoArgsConstructor/@AllArgsConstructor
 * для корректной десериализации Jackson при structured output в Spring AI.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Incident {
    private String category;
    private String description;
    private CriticalityType criticality;
    private User user;
    private List<Hypothesis> hypotheses;
}