package dev.anton_kulakov.controller;

import dev.anton_kulakov.dto.UserMapper;
import dev.anton_kulakov.dto.UserRequestDto;
import dev.anton_kulakov.dto.UserResponseDto;
import dev.anton_kulakov.model.User;
import dev.anton_kulakov.service.FolderService;
import dev.anton_kulakov.service.PathHelper;
import dev.anton_kulakov.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/sign-up")
@RequiredArgsConstructor
public class RegistrationController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final FolderService folderService;
    private final PathHelper pathHelper;

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto userRequestDto,
                                                      HttpServletRequest request) {
        User user = userService.createUser(userRequestDto);
        UserResponseDto userResponseDto = userMapper.toResponseDto(user);
        String userRootFolder = pathHelper.getUserRootFolder(user.getId());
        folderService.create(userRootFolder);
        HttpSession session = request.getSession(true);
        session.setAttribute("userId", user.getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userResponseDto);
    }
}
