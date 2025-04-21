package dev.anton_kulakov.controller;

import dev.anton_kulakov.dto.ResourceInfoDto;
import dev.anton_kulakov.exception.ResourceAlreadyExistsException;
import dev.anton_kulakov.model.SecurityUser;
import dev.anton_kulakov.service.FolderService;
import dev.anton_kulakov.service.PathHelper;
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
    private final PathHelper pathHelper;

    @GetMapping
    public ResponseEntity<List<ResourceInfoDto>> getFolderContent(@AuthenticationPrincipal SecurityUser securityUser,
                                                                  @RequestParam String path) {
        String userRootFolder = pathHelper.getUserRootFolder(securityUser.getUserId());
        List<ResourceInfoDto> resources = folderService.getContent(userRootFolder + path);
        return ResponseEntity.ok(resources);
    }

    @PostMapping
    public ResponseEntity<ResourceInfoDto> create(@AuthenticationPrincipal SecurityUser securityUser,
                                                  @RequestParam String path) {
        String userRootFolder = pathHelper.getUserRootFolder(securityUser.getUserId());

        if (folderService.isExists(userRootFolder + path)) {
            throw new ResourceAlreadyExistsException("The folder with the path %s is already exists".formatted(userRootFolder + path));
        }
        
        folderService.create(userRootFolder + path);
        ResourceInfoDto resourceInfoDto = folderService.getInfo(userRootFolder + path);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resourceInfoDto);
    }
}
