package ru.che.lcp.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Данные клиента, связанного с инцидентом")
public class UserDto {

    @Schema(description = "Уникальный идентификатор клиента", example = "CLT-00123")
    private String id;

    @Schema(description = "Полное имя клиента", example = "Иванов Иван Иванович")
    private String name;

    @Schema(description = "Контактный телефон клиента", example = "+7 (999) 123-45-67")
    private String phone;
}