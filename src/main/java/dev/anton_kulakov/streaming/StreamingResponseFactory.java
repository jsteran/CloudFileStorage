package dev.anton_kulakov.streaming;

import dev.anton_kulakov.exception.ResourceNotFoundException;
import dev.anton_kulakov.service.FolderService;
import dev.anton_kulakov.service.MinioService;
import dev.anton_kulakov.util.PathProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StreamingResponseFactory {
    private final MinioService minioService;
    private final FolderService folderService;
    private final PathProcessor pathProcessor;
    private final StreamCopier streamCopier;

    public StreamingResponseBody createResponse(String path) {
        if (path.endsWith("/")) {
            List<String> resourcesInFolder = folderService.getResourcesNamesInFolder(path);

            if (resourcesInFolder.isEmpty()) {
                log.error("Folder '{}' is empty", path);
                throw new ResourceNotFoundException("Folder is empty");
            }

            return new FolderStreamingResponseBody(minioService, pathProcessor, streamCopier, resourcesInFolder, path);
        }

        return new FileStreamingResponseBody(minioService, streamCopier, path);
    }

    public MediaType getContentType(String path) {
        return path.endsWith("/") ? MediaType.valueOf("application/zip") : MediaType.APPLICATION_OCTET_STREAM;
    }
}
