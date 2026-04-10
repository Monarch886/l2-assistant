package ru.che.lcp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.che.lcp.client.IncidentController;
import ru.che.lcp.client.reqres.IncidentRequest;
import ru.che.lcp.client.view.IncidentView;
import ru.che.lcp.usecase.IncidentUsecase;
import ru.che.lcp.usecase.mapper.IncidentMapper;

@Slf4j
@RestController
@RequiredArgsConstructor
public class IncidentControllerImpl implements IncidentController {

    private final IncidentUsecase useCase;
    private final IncidentMapper mapper;

    @Override
    public ResponseEntity<IncidentView> createIncident(@Valid @RequestBody IncidentRequest request) {
        log.debug("Incoming incident request: {}", request.getIncidentDescription());
        IncidentView response = mapper.toView(useCase.analyze(request.getIncidentDescription()));
        return ResponseEntity.ok(response);
    }
}