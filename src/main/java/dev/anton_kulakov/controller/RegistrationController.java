package dev.anton_kulakov.controller;

import dev.anton_kulakov.config.OpenApiConfig;
import dev.anton_kulakov.dto.ErrorMessage;
import dev.anton_kulakov.dto.UserRequestDto;
import dev.anton_kulakov.dto.UserResponseDto;
import dev.anton_kulakov.mapper.UserMapper;
import dev.anton_kulakov.model.User;
import dev.anton_kulakov.service.MinioService;
import dev.anton_kulakov.service.UserDetailsServiceImpl;
import dev.anton_kulakov.service.UserService;
import dev.anton_kulakov.util.PathProcessor;
import dev.anton_kulakov.util.SecurityContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = OpenApiConfig.REGISTRATION_TAG)
public class RegistrationController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final MinioService minioService;
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final SecurityContextUtil securityContextUtil;
    private final PathProcessor pathProcessor;

    @Operation(summary = "Creating a new user")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDto.class),
                            examples = {
                                    @ExampleObject(
                                            value = """
                                                    {
                                                        "username": "test_username"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class),
                            examples = {
                                    @ExampleObject(
                                            value = """
                                                    {
                                                        "message": "There is a validation error. username: Username should be longer than 5 characters"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Username is already taken",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class),
                            examples = {
                                    @ExampleObject(
                                            value = """
                                                    {
                                                        "message": "User with username test_user is already exists"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class),
                            examples = {
                                    @ExampleObject(
                                            value = """
                                                    {
                                                        "message": "We're sorry, but an unexpected error has occurred. Please try again later"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    @PostMapping("/api/auth/sign-up")
    public ResponseEntity<UserResponseDto> create(@Valid @RequestBody UserRequestDto userRequestDto,
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
