package dev.anton_kulakov.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "DTO for user registration and authentication")
public class UserRequestDto {
    @NotBlank(message = "Username should not be empty")
    @Size(min = 5, message = "Username should be longer than 5 characters")
    @Schema(description = "Username", example = "test_username", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "Password should not be empty")
    @Size(min = 5, message = "Password should be longer than 5 characters")
    @Schema(description = "Password", example = "password12", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
