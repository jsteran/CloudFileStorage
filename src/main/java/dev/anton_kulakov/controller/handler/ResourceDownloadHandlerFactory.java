package dev.anton_kulakov.controller.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResourceDownloadHandlerFactory {
    private final FileDownloadHandler fileDownloadHandler;
    private final FolderDownloadHandler folderDownloadHandler;

    public ResourceDownloadHandler getDownloadHandler(String path) {
        if (path.endsWith("/")) {
            return folderDownloadHandler;
        }

        return fileDownloadHandler;
    }
}
