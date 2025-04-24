package dev.anton_kulakov.service.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResourceHandlerFactory {
    private final FileResourceHandler fileResourceHandler;
    private final FolderResourceHandler folderResourceHandler;
    private final static String MINIO_FOLDER_PATH_MARKER = "/";

    public ResourceHandlerInterface getResourceHandler(String path) {
        if (path.endsWith(MINIO_FOLDER_PATH_MARKER)) {
            return folderResourceHandler;
        }

        return fileResourceHandler;
    }
}
