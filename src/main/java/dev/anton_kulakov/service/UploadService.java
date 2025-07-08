package dev.anton_kulakov.service;

import dev.anton_kulakov.dto.ResourceInfoDto;
import dev.anton_kulakov.service.handler.FileResourceHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UploadService {
    private final MinioService minioService;
    private final FileResourceHandler fileResourceHandler;

    public List<ResourceInfoDto> upload(String path, List<MultipartFile> files) {
        List<ResourceInfoDto> uploadedResources = new ArrayList<>();

        for (MultipartFile file : files) {
            String fullPath = path + file.getOriginalFilename();
            minioService.upload(fullPath, file, true);
            uploadedResources.add(fileResourceHandler.getInfo(fullPath));
        }

        return uploadedResources;
    }
}
