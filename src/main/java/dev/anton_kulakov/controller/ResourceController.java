package dev.anton_kulakov.controller;

import dev.anton_kulakov.config.OpenApiConfig;
import dev.anton_kulakov.config.resolver.FullPath;
import dev.anton_kulakov.dto.DownloadResponse;
import dev.anton_kulakov.dto.ErrorMessage;
import dev.anton_kulakov.dto.ResourceInfoDto;
import dev.anton_kulakov.model.SecurityUser;
import dev.anton_kulakov.service.SearchService;
import dev.anton_kulakov.service.UploadService;
import dev.anton_kulakov.service.ResourceServiceFactory;
import dev.anton_kulakov.service.ResourceServiceInterface;
import dev.anton_kulakov.service.DownloadService;
import dev.anton_kulakov.validation.ValidPath;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@Tag(name = OpenApiConfig.RESOURCE_TAG)
public class ResourceController {
    public final ResourceServiceFactory resourceServiceFactory;
    private final SearchService searchService;
    private final DownloadService downloadService;
    private final UploadService uploadService;

    @Operation(
            summary = "Getting information about a file or folder",
            description = "Provides the user with information about the path to a resource, its name, and type. It also displays the file size.")
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
                    description = "The path is invalid or not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class),
                            examples = {
                                    @ExampleObject(
                                            value = """
                                                    {
                                                        "message": "The path is invalid or not found"
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
                    description = "Resource not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class),
                            examples = {
                                    @ExampleObject(
                                            value = """
                                                    {
                                                        "message": "The requested resource could not be found"
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
                    ))
    })
    @GetMapping("/api/resource")
    public ResponseEntity<ResourceInfoDto> getInfo(
            @FullPath("path")
            @ValidPath
            @Parameter(description = "The path to the folder or file", example = "folder/file.txt") String path) {
        ResourceServiceInterface resourceHandler = resourceServiceFactory.getResourceHandler(path);
        ResourceInfoDto resourceInfoDto = resourceHandler.getInfo(path);

        return ResponseEntity.ok().body(resourceInfoDto);
    }

    @Operation(summary = "Downloading a folder or a file")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = {
                            @Content(mediaType = "application/octet-stream"),
                            @Content(mediaType = "application/zip")
                    }
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
                    description = "Resource not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class),
                            examples = {
                                    @ExampleObject(
                                            value = """
                                                    {
                                                        "message": "The requested resource could not be found"
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
                    ))
    })
    @GetMapping("/api/resource/download")
    public ResponseEntity<StreamingResponseBody> download(
            @FullPath("path")
            @ValidPath
            @Parameter(description = "The path to the folder or file you want to download", example = "folder/file.txt") String path) {
        DownloadResponse downloadResponse = downloadService.prepareDownloadResponse(path);

        return ResponseEntity.ok()
                .contentType(downloadResponse.getContentType())
                .body(downloadResponse.getResponseBody());
    }

    @Operation(summary = "Deleting a folder or a file")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "No Content"
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
                    description = "Resource not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class),
                            examples = {
                                    @ExampleObject(
                                            value = """
                                                    {
                                                        "message": "The requested resource could not be found"
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
                    ))
    })
    @DeleteMapping("/api/resource")
    public ResponseEntity<Void> delete(
            @FullPath("path")
            @ValidPath
            @Parameter(description = "The path to the folder or file that you want to delete", example = "folder/file.txt") String path) {
        ResourceServiceInterface resourceHandler = resourceServiceFactory.getResourceHandler(path);
        resourceHandler.delete(path);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Move or rename a folder or a file")
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
                    description = "Resource not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class),
                            examples = {
                                    @ExampleObject(
                                            value = """
                                                    {
                                                        "message": "The requested resource could not be found"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "The resource already exists at the destination path",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class),
                            examples = {
                                    @ExampleObject(
                                            value = """
                                                    {
                                                        "message": "The resource already exists at the destination path"
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
                    ))
    })
    @GetMapping("/api/resource/move")
    public ResponseEntity<ResourceInfoDto> move(
            @FullPath("from")
            @ValidPath
            @Parameter(description = "The path to the folder or file that we want to rename or move", example = "folder/file.txt") String from,
            @FullPath("to")
            @ValidPath
            @Parameter(description = "The new path to the folder or file that we are moving or renaming", example = "folder/new_file.txt") String to) {
        ResourceServiceInterface resourceHandler = resourceServiceFactory.getResourceHandler(from);
        String newPath = resourceHandler.move(from, to);
        ResourceInfoDto resourceInfoDto = resourceHandler.getInfo(newPath);

        return ResponseEntity.ok().body(resourceInfoDto);
    }

    @Operation(summary = "Search for files and folders that match the user's search criteria")
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
                    ))
    })
    @GetMapping("/api/resource/search")
    public ResponseEntity<List<ResourceInfoDto>> search(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestParam
            @Parameter(description = "The name of the folder or file the user is searching for", example = "picture") String query) {
        List<ResourceInfoDto> resources = searchService.search(securityUser.getUserId(), query.toLowerCase());
        return ResponseEntity.ok().body(resources);
    }

    @Operation(summary = "Uploading folders and files to the cloud storage")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResourceInfoDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class),
                            examples = {
                                    @ExampleObject(
                                            value = """
                                                    {
                                                        "message": "There is a validation error. The request body is invalid"
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
                    responseCode = "409",
                    description = "The resource already exists at the destination path",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorMessage.class),
                            examples = {
                                    @ExampleObject(
                                            value = """
                                                    {
                                                        "message": "The resource already exists at the destination path"
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
                    ))
    })
    @PostMapping("/api/resource")
    public ResponseEntity<List<ResourceInfoDto>> upload(
            @FullPath("path")
            @ValidPath
            @Parameter(description = "The path to the folder where the files or the other folder will be uploaded", example = "folder/") String path,
            @Size(min = 1)
            @RequestParam("object")
            @Parameter(description = "list of files to download") List<MultipartFile> files) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(uploadService.upload(path, files));
    }
}
