package dev.anton_kulakov.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResourceServiceFactory {
    private final FileResourceService fileResourceService;
    private final FolderResourceService folderResourceService;
    private final static String MINIO_FOLDER_PATH_MARKER = "/";

    public ResourceServiceInterface getResourceService(String path) {
        if (path.endsWith(MINIO_FOLDER_PATH_MARKER)) {
            return folderResourceService;
        }

        return fileResourceService;
    }
}
