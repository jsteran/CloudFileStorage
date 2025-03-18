package dev.anton_kulakov.controller;

import dev.anton_kulakov.dto.ResourceInfoDto;
import dev.anton_kulakov.dto.StreamingResponseFactory;
import dev.anton_kulakov.service.ResourceServiceFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequiredArgsConstructor
public class ResourceController {
    private final ResourceServiceFactory resourceServiceFactory;
    private final StreamingResponseFactory streamingResponseFactory;

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
}
