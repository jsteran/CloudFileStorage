package dev.anton_kulakov.controller.handler;

import dev.anton_kulakov.exception.ResourceNotFoundException;
import dev.anton_kulakov.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FolderDownloadHandler implements ResourceDownloadHandler {
    private final MinioService minioService;
    @Override
    public ResponseEntity<StreamingResponseBody> download(String path) {
        List<String> objectsNamesInFolder = minioService.getObjectsNamesInFolder(path);

        if (objectsNamesInFolder.isEmpty()) {
            throw new ResourceNotFoundException("Folder is empty");
        }

        FolderStreamingResponseBody responseBody = new FolderStreamingResponseBody(minioService, objectsNamesInFolder, path);
        return ResponseEntity.ok().contentType(MediaType.valueOf("application/zip")).body(responseBody);
    }
}
