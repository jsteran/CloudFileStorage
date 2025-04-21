package dev.anton_kulakov.controller;

import dev.anton_kulakov.dto.ResourceInfoDto;
import dev.anton_kulakov.dto.StreamingResponseFactory;
import dev.anton_kulakov.exception.ResourceAlreadyExistsException;
import dev.anton_kulakov.model.SecurityUser;
import dev.anton_kulakov.service.MinioHelper;
import dev.anton_kulakov.service.PathHelper;
import dev.anton_kulakov.service.ResourceServiceFactory;
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
    private final ResourceServiceFactory resourceServiceFactory;
    private final StreamingResponseFactory streamingResponseFactory;
    private final PathHelper pathHelper;
    private final MinioHelper minioHelper;

    @GetMapping("/api/resource")
    public ResponseEntity<ResourceInfoDto> getInfo(@RequestParam String path) {
        ResourceInfoDto resourceInfoDto = resourceServiceFactory.getService(path).getInfo(path);
        return ResponseEntity.ok().body(resourceInfoDto);
    }

    @GetMapping("/api/resource/download")
    public ResponseEntity<StreamingResponseBody> download(@RequestParam String path) {
        return ResponseEntity.ok()
                .contentType(streamingResponseFactory.getContentType(path))
                .body(streamingResponseFactory.createResponse(path));
    }

    @DeleteMapping("/api/resource")
    public ResponseEntity<Void> delete(@RequestParam String path) {
        resourceServiceFactory.getService(path).delete(path);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/resource/move")
    public ResponseEntity<ResourceInfoDto> move(@AuthenticationPrincipal SecurityUser securityUser,
                                                @RequestParam String from,
                                                @RequestParam String to) {
        String userRootFolder = pathHelper.getUserRootFolder(securityUser.getUserId());

        if (!from.contains(userRootFolder)) {
            from = userRootFolder + from;
        }

        if (!to.contains(userRootFolder)) {
            to = userRootFolder + to;
        }

        resourceServiceFactory.getService(from).move(from, to);
        ResourceInfoDto resourceInfoDto = resourceServiceFactory.getService(to).getInfo(to);
        return ResponseEntity.ok().body(resourceInfoDto);
    }

    @GetMapping("/api/resource/search")
    public ResponseEntity<List<ResourceInfoDto>> search(@AuthenticationPrincipal SecurityUser securityUser,
                                                        @RequestParam String query) {
        String userRootFolder = pathHelper.getUserRootFolder(securityUser.getUserId());
        List<ResourceInfoDto> resources = minioHelper.search(userRootFolder, query.toLowerCase());
        return ResponseEntity.ok().body(resources);
    }

    @PostMapping("/api/resource")
    public ResponseEntity<List<ResourceInfoDto>> upload(@AuthenticationPrincipal SecurityUser securityUser,
                                                        @RequestParam String path,
                                                        @RequestParam("object") List<MultipartFile> files) {
        String userRootFolder = pathHelper.getUserRootFolder(securityUser.getUserId());
        List<ResourceInfoDto> resourceInfoDtos = new ArrayList<>();

        for (MultipartFile file : files) {
            if (resourceServiceFactory.getService(file.getOriginalFilename()).isExists(userRootFolder + file.getOriginalFilename())) {
                throw new ResourceAlreadyExistsException("The file with the path %s is already exists".formatted(userRootFolder + file.getOriginalFilename()));
            }

            resourceServiceFactory.getFileUploadService().upload(userRootFolder + path, file);
            String newPath = userRootFolder + path + file.getOriginalFilename();
            ResourceInfoDto resourceInfoDto = resourceServiceFactory.getService(newPath).getInfo(newPath);
            resourceInfoDtos.add(resourceInfoDto);
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resourceInfoDtos);
    }
}
