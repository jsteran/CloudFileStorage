package dev.anton_kulakov.controller;

import dev.anton_kulakov.dto.ResourceInfoDto;
import dev.anton_kulakov.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceController {
    private final MinioService minioService;

    @GetMapping
    public ResponseEntity<ResourceInfoDto> getResourceInfo(@RequestParam String path) {
        ResourceInfoDto resourceInfoDto = minioService.getResourceInfoDto(path);
        return ResponseEntity.ok(resourceInfoDto);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteResource(@RequestParam String path) {
        minioService.deleteResource(path);
        return ResponseEntity.noContent().build();
    }
}
