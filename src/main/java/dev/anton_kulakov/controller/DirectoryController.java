package dev.anton_kulakov.controller;

import dev.anton_kulakov.dto.ResourceInfoDto;
import dev.anton_kulakov.service.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequiredArgsConstructor
public class DirectoryController {
    private final FolderService folderService;
    @GetMapping("/api/directory")
    public ResponseEntity<ArrayList<ResourceInfoDto>> getFolderContent(@RequestParam String path) {
        ArrayList<ResourceInfoDto> resources = folderService.getContent(path);
        return ResponseEntity.ok(resources);
    }
}
