package dev.anton_kulakov.controller;

import dev.anton_kulakov.config.OpenApiConfig;
import dev.anton_kulakov.dto.UserResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@Tag(name = OpenApiConfig.USER_TAG)
public class UserController {
    @Operation(summary = "Getting the current user")
    @GetMapping("/api/user/me")
    public ResponseEntity<UserResponseDto> getUser(Principal principal) {
        return ResponseEntity.ok(new UserResponseDto(principal.getName()));
    }
}
