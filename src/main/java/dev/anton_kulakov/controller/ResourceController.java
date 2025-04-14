package dev.anton_kulakov.controller;

import dev.anton_kulakov.dto.ResourceInfoDto;
import dev.anton_kulakov.dto.StreamingResponseFactory;
import dev.anton_kulakov.model.SecurityUser;
import dev.anton_kulakov.service.FileService;
import dev.anton_kulakov.service.PathHelper;
import dev.anton_kulakov.service.ResourceServiceFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ResourceController {
    private final ResourceServiceFactory resourceServiceFactory;
    private final StreamingResponseFactory streamingResponseFactory;
    private final FileService fileService;
    private final PathHelper pathHelper;

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
    public ResponseEntity<Void> delete(@AuthenticationPrincipal SecurityUser securityUser,
                                       @RequestParam String path) {
        String userRootFolder = pathHelper.getUserRootFolder(securityUser.getUserId());
        resourceServiceFactory.getService(path).delete(userRootFolder + path);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/resource/move")
    public ResponseEntity<ResourceInfoDto> move(@RequestParam String from, @RequestParam String to) {
        resourceServiceFactory.getService(from).move(from, to);
        ResourceInfoDto resourceInfoDto = resourceServiceFactory.getService(to).getInfo(to);
        return ResponseEntity.ok().body(resourceInfoDto);
    }

    @GetMapping("/api/resource/search")
    public ResponseEntity<List<ResourceInfoDto>> search(@RequestParam String query) {
        List<ResourceInfoDto> resources = fileService.search(query);
        return ResponseEntity.ok().body(resources);
    }

    @PostMapping("/api/resource")
    public ResponseEntity<ResourceInfoDto> upload(@AuthenticationPrincipal SecurityUser securityUser,
                                                  @RequestParam String path,
                                                  @RequestParam MultipartFile object) {
        String userRootFolder = pathHelper.getUserRootFolder(securityUser.getUserId());
        fileService.upload(userRootFolder + path, object);
        String newPath = userRootFolder + path + object.getOriginalFilename();
        ResourceInfoDto resourceInfoDto = resourceServiceFactory.getService(newPath).getInfo(newPath);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resourceInfoDto);
    }
}
