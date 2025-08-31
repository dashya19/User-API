package com.example.userapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequestDTO {
    @NotBlank(message = "ФИО является обязательным")
    @Size(min = 2, max = 255, message = "ФИО должно содержать от 2 до 255 символов")
    private String fio;

    @NotBlank(message = "Номер телефона является обязательным")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Номер телефона должен быть указан в действующем международном формате")
    private String phoneNumber;

    @URL(message = "У аватара должен быть действительный URL-адрес")
    private String avatar;

    @NotBlank(message = "Название роли является обязательным")
    @Size(min = 2, max = 50, message = "Название роли должно содержать от 2 до 50 символов")
    private String roleName;
}
