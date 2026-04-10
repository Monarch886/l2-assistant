package ru.che.lcp.usecase.mapper;

import org.mapstruct.Mapper;
import ru.che.lcp.client.dto.HypothesisDto;
import ru.che.lcp.domain.Hypothesis;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;
import static org.mapstruct.ReportingPolicy.ERROR;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ERROR,
        nullValueCheckStrategy = ALWAYS,
        nullValuePropertyMappingStrategy = IGNORE)
interface HypothesisMapper {
    HypothesisDto toDto(Hypothesis model);
}