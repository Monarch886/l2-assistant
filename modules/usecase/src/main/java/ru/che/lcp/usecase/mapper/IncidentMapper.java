package ru.che.lcp.usecase.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.che.lcp.aiclient.md.IncidentMr;
import ru.che.lcp.client.view.IncidentView;
import ru.che.lcp.domain.Incident;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;
import static org.mapstruct.ReportingPolicy.ERROR;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ERROR,
        nullValueCheckStrategy = ALWAYS,
        nullValuePropertyMappingStrategy = IGNORE,
        uses = {HypothesisMapper.class, UserMapper.class})
public interface IncidentMapper {
    IncidentView toView(Incident model);

    @Mapping(target = "user", ignore = true)
    Incident toModel(IncidentMr mr);
}