package ru.che.lcp.aiclient.md;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.che.lcp.domain.Hypothesis;
import ru.che.lcp.domain.enums.CriticalityType;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentMr {
    private String category;
    private String description;
    private CriticalityType criticality;
    private List<Hypothesis> hypotheses;
}
