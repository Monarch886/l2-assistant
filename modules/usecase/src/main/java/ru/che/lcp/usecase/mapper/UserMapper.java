package ru.che.lcp.usecase.mapper;

import org.mapstruct.Mapper;
import ru.che.lcp.client.dto.UserDto;
import ru.che.lcp.domain.User;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;
import static org.mapstruct.ReportingPolicy.ERROR;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ERROR,
        nullValueCheckStrategy = ALWAYS,
        nullValuePropertyMappingStrategy = IGNORE)
interface UserMapper {
    UserDto toDto(User model);
}