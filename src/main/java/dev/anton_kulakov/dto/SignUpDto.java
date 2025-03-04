package dev.anton_kulakov.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SignUpDto {
    @NotEmpty(message = "Имя пользователя не должно быть пустым")
    @Size(min = 2, message = "Минимальная длина имени пользователя 5 символов")
    private String username;

    @NotEmpty(message = "Пароль не должен быть пустым")
    @Size(min = 2, message = "Минимальная длина пароля 5 символов")
    private String password;

    @NotEmpty(message = "Подтверждение пароля не должно быть пустым")
    @Size(min = 2, message = "Минимальная длина подтверждения пароля 5 символов")
    private String confirmPassword;
}
