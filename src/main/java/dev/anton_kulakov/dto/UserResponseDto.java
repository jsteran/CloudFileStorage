package dev.anton_kulakov.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "DTO for displaying the user name")
public class UserResponseDto {
    @Schema(description = "Username", example = "test_username")
    private String username;
}
