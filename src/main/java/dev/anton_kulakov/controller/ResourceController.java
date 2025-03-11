package dev.anton_kulakov.controller;

import dev.anton_kulakov.dto.ResourceInfoDto;
import dev.anton_kulakov.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
