package com.example.userapi.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequestDTO {
    private UUID id;

    @Size(min = 2, max = 255, message = "ФИО должно содержать от 2 до 255 символов")
    private String fio;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Номер телефона должен быть указан в действующем международном формате")
    private String phoneNumber;

    @URL(message = "У аватара должен быть действительный URL-адрес")
    private String avatar;

    @Size(min = 2, max = 50, message = "Название роли должно содержать от 2 до 50 символов")
    private String roleName;
}
