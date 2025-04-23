package dev.anton_kulakov.controller;

import dev.anton_kulakov.dto.UserRequestDto;
import dev.anton_kulakov.dto.UserResponseDto;
import dev.anton_kulakov.mapper.UserMapper;
import dev.anton_kulakov.model.User;
import dev.anton_kulakov.service.FolderService;
import dev.anton_kulakov.service.UserService;
import dev.anton_kulakov.util.PathProcessor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final FolderService folderService;
    private final PathProcessor pathProcessor;

    @GetMapping("/api/user/me")
    public ResponseEntity<UserResponseDto> getUser(Principal principal) {
        return ResponseEntity.ok(new UserResponseDto(principal.getName()));
    }

    @PostMapping("/api/auth/sign-up")
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto userRequestDto,
                                                      HttpServletRequest request) {
        User user = userService.createUser(userRequestDto);
        UserResponseDto userResponseDto = userMapper.toResponseDto(user);
        String userRootFolder = pathProcessor.getUserRootFolder(user.getId());
        folderService.create(userRootFolder);
        HttpSession session = request.getSession(true);
        session.setAttribute("userId", user.getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userResponseDto);
    }
}
