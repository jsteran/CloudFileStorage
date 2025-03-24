package dev.anton_kulakov.controller;

import dev.anton_kulakov.dto.ResourceInfoDto;
import dev.anton_kulakov.service.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/directory")
public class DirectoryController {
    private final FolderService folderService;
    @GetMapping
    public ResponseEntity<ArrayList<ResourceInfoDto>> getFolderContent(@RequestParam String path) {
        ArrayList<ResourceInfoDto> resources = folderService.getContent(path);
        return ResponseEntity.ok(resources);
    }

    @PostMapping
    public ResponseEntity<ResourceInfoDto> create(@RequestParam String path) {
        folderService.create(path);
        ResourceInfoDto resourceInfoDto = folderService.getInfo(path);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resourceInfoDto);
    }
}
