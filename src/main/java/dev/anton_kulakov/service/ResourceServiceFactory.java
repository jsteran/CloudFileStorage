package dev.anton_kulakov.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResourceServiceFactory {
    private final FileService fileService;
    private final FolderService folderService;
    private final ResourceSearchService resourceSearchService;

    public ResourceServiceInterface getService(String resourcePath) {
        return resourcePath.endsWith("/") ? folderService : fileService;
    }

    public FileService getFileUploadService() {
        return fileService;
    }

    public ResourceSearchService getSearchService() {
        return resourceSearchService;
    }
}
