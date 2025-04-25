package dev.anton_kulakov.controller;

import dev.anton_kulakov.dto.UserRequestDto;
import dev.anton_kulakov.dto.UserResponseDto;
import dev.anton_kulakov.mapper.UserMapper;
import dev.anton_kulakov.model.User;
import dev.anton_kulakov.service.MinioService;
import dev.anton_kulakov.service.UserDetailsServiceImpl;
import dev.anton_kulakov.service.UserService;
import dev.anton_kulakov.util.PathProcessor;
import dev.anton_kulakov.util.SecurityContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final MinioService minioService;
    private final PathProcessor pathProcessor;
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final SecurityContextUtil securityContextUtil;

    @GetMapping("/api/user/me")
    public ResponseEntity<UserResponseDto> getUser(Principal principal) {
        return ResponseEntity.ok(new UserResponseDto(principal.getName()));
    }

    @PostMapping("/api/auth/sign-up")
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto userRequestDto,
                                                      HttpServletRequest req,
                                                      HttpServletResponse resp) {
        User user = userService.createUser(userRequestDto);
        UserResponseDto userResponseDto = userMapper.toResponseDto(user);
        String userRootFolder = pathProcessor.getUserRootFolder(user.getId());
        minioService.createEmptyFolder(userRootFolder);

        UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(user.getUsername());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        securityContextUtil.setAuthentication(authentication, req, resp);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userResponseDto);
    }
}
