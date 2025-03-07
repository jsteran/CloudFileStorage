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
public class UserRequestDto {
    @NotEmpty(message = "Username should not be empty")
    @Size(min = 5, message = "Username should be longer than 5 characters")
    private String username;

    @NotEmpty(message = "Password should not be empty")
    @Size(min = 5, message = "Password should be longer than 5 characters")
    private String password;
}
