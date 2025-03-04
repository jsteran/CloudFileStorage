package dev.anton_kulakov.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.anton_kulakov.dto.SignUpDto;
import dev.anton_kulakov.model.User;
import dev.anton_kulakov.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth/sign-up")
@RequiredArgsConstructor
public class RegistrationController {
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @PostMapping
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        SignUpDto signUpDto = objectMapper.readValue(req.getInputStream(), SignUpDto.class);
        User user = userService.createUser(signUpDto);

        resp.setStatus(HttpStatus.CREATED.value());
        resp.getWriter().write("{\"username\": \"" + user.getUsername() + "\"}");
    }
}
