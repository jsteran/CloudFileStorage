package dev.anton_kulakov.controller;

import dev.anton_kulakov.dto.ResourceInfoDto;
import dev.anton_kulakov.exception.ResourceAlreadyExistsException;
import dev.anton_kulakov.model.SecurityUser;
import dev.anton_kulakov.service.ResourceSearchService;
import dev.anton_kulakov.service.handler.ResourceHandlerFactory;
import dev.anton_kulakov.service.handler.ResourceHandlerInterface;
import dev.anton_kulakov.streaming.StreamingResponseFactory;
import dev.anton_kulakov.util.PathProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ResourceController {
    public final ResourceHandlerFactory resourceHandlerFactory;
    private final ResourceSearchService resourceSearchService;
    private final StreamingResponseFactory streamingResponseFactory;
    private final PathProcessor pathProcessor;

    @GetMapping("/api/resource")
    public ResponseEntity<ResourceInfoDto> getInfo(@RequestParam String path) {
        ResourceHandlerInterface resourceHandler = resourceHandlerFactory.getResourceHandler(path);
        ResourceInfoDto resourceInfoDto = resourceHandler.getInfo(path);
        return ResponseEntity.ok().body(resourceInfoDto);
    }

    @GetMapping("/api/resource/download")
    public ResponseEntity<StreamingResponseBody> download(@RequestParam String path) {
        resourceHandlerFactory.getResourceHandler(path).getInfo(path);
        return ResponseEntity.ok()
                .contentType(streamingResponseFactory.getContentType(path))
                .body(streamingResponseFactory.createResponse(path));
    }

    @DeleteMapping("/api/resource")
    public ResponseEntity<Void> delete(@RequestParam String path) {
        ResourceHandlerInterface resourceHandler = resourceHandlerFactory.getResourceHandler(path);
        resourceHandler.delete(path);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/resource/move")
    public ResponseEntity<ResourceInfoDto> move(@AuthenticationPrincipal SecurityUser securityUser,
                                                @RequestParam String from,
                                                @RequestParam String to) {
        String userRootFolder = pathProcessor.getUserRootFolder(securityUser.getUserId());
        String fromPath = pathProcessor.getPathWithUserRootFolder(from, userRootFolder);
        String toPath = pathProcessor.getPathWithUserRootFolder(to, userRootFolder);
        ResourceHandlerInterface resourceHandler = resourceHandlerFactory.getResourceHandler(fromPath);
        resourceHandler.move(fromPath, toPath);
        ResourceInfoDto resourceInfoDto = resourceHandler.getInfo(toPath);
        return ResponseEntity.ok().body(resourceInfoDto);
    }

    @GetMapping("/api/resource/search")
    public ResponseEntity<List<ResourceInfoDto>> search(@AuthenticationPrincipal SecurityUser securityUser,
                                                        @RequestParam String query) {
        String userRootFolder = pathProcessor.getUserRootFolder(securityUser.getUserId());
        List<ResourceInfoDto> resources = resourceSearchService.search(userRootFolder, query.toLowerCase());
        return ResponseEntity.ok().body(resources);
    }

    @PostMapping("/api/resource")
    public ResponseEntity<List<ResourceInfoDto>> upload(@AuthenticationPrincipal SecurityUser securityUser,
                                                        @RequestParam String path,
                                                        @RequestParam("object") List<MultipartFile> files) {
        String userRootFolder = pathProcessor.getUserRootFolder(securityUser.getUserId());
        List<ResourceInfoDto> resourceInfoDtos = new ArrayList<>();

        for (MultipartFile file : files) {
            String fullPath = userRootFolder + file.getOriginalFilename();
            ResourceHandlerInterface resourceHandler = resourceHandlerFactory.getResourceHandler(fullPath);

            if (resourceHandler.isExists(fullPath)) {
                throw new ResourceAlreadyExistsException("The file with the path %s is already exists".formatted(fullPath));
            }

            ResourceInfoDto resourceInfoDto = resourceHandler.upload(userRootFolder + path, file);
            resourceInfoDtos.add(resourceInfoDto);
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resourceInfoDtos);
    }
}
