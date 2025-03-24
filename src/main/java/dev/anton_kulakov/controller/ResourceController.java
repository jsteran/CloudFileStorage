package dev.anton_kulakov.controller;

import dev.anton_kulakov.dto.ResourceInfoDto;
import dev.anton_kulakov.dto.StreamingResponseFactory;
import dev.anton_kulakov.service.FileService;
import dev.anton_kulakov.service.ResourceServiceFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ResourceInfoDto> upload(@RequestParam String path, @RequestParam MultipartFile file) {
        fileService.upload(path, file);
        String newPath = path + file.getOriginalFilename();
        ResourceInfoDto resourceInfoDto = resourceServiceFactory.getService(newPath).getInfo(newPath);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resourceInfoDto);
    }
}
