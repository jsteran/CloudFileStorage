package dev.anton_kulakov.controller;

import dev.anton_kulakov.dto.ResourceInfoDto;
import dev.anton_kulakov.exception.ResourceAlreadyExistsException;
import dev.anton_kulakov.exception.ResourceNotFoundException;
import dev.anton_kulakov.model.SecurityUser;
import dev.anton_kulakov.service.FolderService;
import dev.anton_kulakov.service.MinioService;
import dev.anton_kulakov.util.PathProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/directory")
public class FolderController {
    private final FolderService folderService;
    private final MinioService minioService;
    private final PathProcessor pathProcessor;

    @GetMapping
    public ResponseEntity<List<ResourceInfoDto>> getFolderContent(@AuthenticationPrincipal SecurityUser securityUser,
                                                                  @RequestParam String path) {
        String userRootFolder = pathProcessor.getUserRootFolder(securityUser.getUserId());
        List<ResourceInfoDto> resources = folderService.getContent(userRootFolder + path);
        return ResponseEntity.ok(resources);
    }

    @PostMapping
    public ResponseEntity<ResourceInfoDto> create(@AuthenticationPrincipal SecurityUser securityUser,
                                                  @RequestParam String path) {
        String userRootFolder = pathProcessor.getUserRootFolder(securityUser.getUserId());
        String fullPath = userRootFolder + path;
        String newFolderName = pathProcessor.getLastFolderName(fullPath);
        String parentFolderPath = pathProcessor.getPathWithoutLastFolder(fullPath, newFolderName);

        if (!minioService.isFolderExists(parentFolderPath)) {
            throw new ResourceNotFoundException("The parent folder doesn't exists");
        }

        if (minioService.isFolderExists(fullPath)) {
            throw new ResourceAlreadyExistsException("The folder with the path %s is already exists".formatted(fullPath));
        }

        minioService.createEmptyFolder(fullPath);
        ResourceInfoDto resourceInfoDto = folderService.getInfo(fullPath);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resourceInfoDto);
    }
}
