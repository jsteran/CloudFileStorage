package dev.anton_kulakov.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResourceServiceFactory {
    private final FileResourceService fileResourceHandler;
    private final FolderResourceService folderResourceHandler;
    private final static String MINIO_FOLDER_PATH_MARKER = "/";

    public ResourceServiceInterface getResourceHandler(String path) {
        if (path.endsWith(MINIO_FOLDER_PATH_MARKER)) {
            return folderResourceHandler;
        }

        return fileResourceHandler;
    }
}
