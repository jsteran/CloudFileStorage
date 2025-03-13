package dev.anton_kulakov.controller;

import dev.anton_kulakov.dto.ResourceInfoDto;
import dev.anton_kulakov.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequiredArgsConstructor
public class ResourceController {
    private final MinioService minioService;

    @GetMapping("/api/resource")
    public ResponseEntity<ResourceInfoDto> getResourceInfo(@RequestParam String path) {
        ResourceInfoDto resourceInfoDto = minioService.getResourceInfoDto(path);
        return ResponseEntity.ok(resourceInfoDto);
    }

    @GetMapping("/api/resource/download")
    public ResponseEntity<StreamingResponseBody> downloadResource(@RequestParam String path) {
        ResourceStreamingResponseBody responseBody = new ResourceStreamingResponseBody(minioService, path);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(responseBody);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteResource(@RequestParam String path) {
        minioService.deleteResource(path);
        return ResponseEntity.noContent().build();
    }
}
