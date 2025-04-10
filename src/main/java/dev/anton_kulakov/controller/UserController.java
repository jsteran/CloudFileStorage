package dev.anton_kulakov.controller;

import dev.anton_kulakov.dto.UserResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/user/me")
public class UserController {
    @GetMapping
    public ResponseEntity<UserResponseDto> getUser(Principal principal) {
        return ResponseEntity.ok(new UserResponseDto(principal.getName()));
    }
}
