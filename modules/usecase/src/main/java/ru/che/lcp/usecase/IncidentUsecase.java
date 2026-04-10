package ru.che.lcp.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.che.lcp.aiclient.AnalysisPort;
import ru.che.lcp.domain.Incident;
import ru.che.lcp.usecase.mapper.IncidentMapper;

import static ru.che.lcp.usecase.util.PromptInjectionUtil.check;
import static ru.che.lcp.usecase.util.SensitiveDataUtil.sanitize;

@Service
@RequiredArgsConstructor
public class IncidentUsecase {
    private final AnalysisPort analysisPort;
    private final IncidentMapper mapper;

    public Incident analyze(String incidentDescription) {
        check(incidentDescription);                        // 1. блокировать injection до любой обработки
        String sanitized = sanitize(incidentDescription);  // 2. вычистить чувствительные данные
        return mapper.toModel(analysisPort.analyze(sanitized));
    }
}