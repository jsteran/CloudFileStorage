package dev.anton_kulakov.controller;

import dev.anton_kulakov.config.OpenApiConfig;
import dev.anton_kulakov.dto.ErrorMessage;
import dev.anton_kulakov.dto.ResourceInfoDto;
import dev.anton_kulakov.exception.ResourceAlreadyExistsException;
import dev.anton_kulakov.exception.ResourceNotFoundException;
import dev.anton_kulakov.model.SecurityUser;
import dev.anton_kulakov.service.FolderService;
import dev.anton_kulakov.service.MinioService;
import dev.anton_kulakov.service.handler.FolderResourceHandler;
import dev.anton_kulakov.util.PathProcessor;
import dev.anton_kulakov.validation.ValidPath;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/directory")
@Tag(name = OpenApiConfig.FOLDER_TAG)
public class FolderController {
    private final FolderService folderService;
    private final FolderResourceHandler folderResourceHandler;
    private final MinioService minioService;
    private final PathProcessor pathProcessor;

    @Operation(summary = "Getting folder contents")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResourceInfoDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "The path is invalid",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class),
                            examples = {
                                    @ExampleObject(
                                            value = """
                                                    {
                                                        "message": "There is a validation error. The path is invalid"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User is not authenticated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class),
                            examples = {
                                    @ExampleObject(
                                            value = """
                                                    {
                                                        "message": "Invalid credentials"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The folder does not exist",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class),
                            examples = {
                                    @ExampleObject(
                                            value = """
                                                    {
                                                        "message": "The requested folder could not be found"
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
    @GetMapping
    public ResponseEntity<List<ResourceInfoDto>> getFolderContent(
            @AuthenticationPrincipal SecurityUser securityUser,
            @ValidPath
            @RequestParam
            @Parameter(description = "The path to the folder containing the content the user is interested in", example = "folder/") String path) {
        String userRootFolder = pathProcessor.getUserRootFolder(securityUser.getUserId());

        if (!folderResourceHandler.isExists(userRootFolder + path)) {
            log.error("The folder with path {} does not exist", userRootFolder + path);
            throw new ResourceNotFoundException("The folder does not exist");
        }

        List<ResourceInfoDto> resources = folderService.getContent(userRootFolder + path);
        return ResponseEntity.ok(resources);
    }

    @Operation(summary = "Creating an empty folder")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResourceInfoDto.class),
                            examples = {
                                    @ExampleObject(
                                            value = """
                                                    {
                                                      "path": "folder/nested_folder/",
                                                      "name": "nested_folder",
                                                      "type": "DIRECTORY"
                                                    }"""
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "The path is invalid",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class),
                            examples = {
                                    @ExampleObject(
                                            value = """
                                                    {
                                                        "message": "There is a validation error. The path is invalidr"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User is not authenticated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class),
                            examples = {
                                    @ExampleObject(
                                            value = """
                                                    {
                                                        "message": "Invalid credentials"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The folder does not exist",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class),
                            examples = {
                                    @ExampleObject(
                                            value = """
                                                    {
                                                        "message": "The requested folder could not be found"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "The folder is already exists",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class),
                            examples = {
                                    @ExampleObject(
                                            value = """
                                                    {
                                                        "message": "The folder with the path folder/nested_folder/ is already exist"
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
    @PostMapping
    public ResponseEntity<ResourceInfoDto> create(
            @AuthenticationPrincipal SecurityUser securityUser,
            @ValidPath
            @RequestParam
            @Parameter(description = "The path where the new folder will be created", example = "folder/new folder/") String path) {
        String userRootFolder = pathProcessor.getUserRootFolder(securityUser.getUserId());
        String fullPath = userRootFolder + path;
        String newFolderName = pathProcessor.getLastFolderName(fullPath);
        String parentFolderPath = pathProcessor.getPathWithoutLastFolder(fullPath, newFolderName);

        if (!parentFolderPath.equals(userRootFolder) && !minioService.isFolderExists(parentFolderPath)) {
            log.error("The parent folder with name {} does not exist", parentFolderPath);
            throw new ResourceNotFoundException("The parent folder doesn't exists");
        }

        if (minioService.isFolderExists(fullPath)) {
            log.error("The folder with path {} is already exists", fullPath);
            throw new ResourceAlreadyExistsException("The folder with the path %s is already exists".formatted(fullPath));
        }

        minioService.createEmptyFolder(fullPath);
        ResourceInfoDto resourceInfoDto = folderService.getInfo(fullPath);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resourceInfoDto);
    }
}
