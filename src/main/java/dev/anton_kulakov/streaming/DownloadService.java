package dev.anton_kulakov.streaming;

import dev.anton_kulakov.dto.DownloadResponse;
import dev.anton_kulakov.exception.ResourceNotFoundException;
import dev.anton_kulakov.service.FolderService;
import dev.anton_kulakov.service.MinioService;
import dev.anton_kulakov.service.handler.ResourceHandlerFactory;
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
public class DownloadService {
    private final MinioService minioService;
    private final FolderService folderService;
    private final ResourceHandlerFactory resourceHandlerFactory;
    private final PathProcessor pathProcessor;
    private final StreamCopier streamCopier;

    public DownloadResponse prepareDownloadResponse(String path) {
        if (!resourceHandlerFactory.getResourceHandler(path).isExists(path)) {
            log.warn("Attempt to download a non-existent resource: {}", path);
            throw new ResourceNotFoundException("The requested resource could not be found");
        }

        StreamingResponseBody responseBody;
        MediaType contentType;

        if (path.endsWith("/")) {
            List<String> resourcesInFolder = folderService.getResourcesNamesInFolder(path);
            responseBody = new FolderStreamingResponseBody(minioService, pathProcessor, streamCopier, resourcesInFolder, path);
            contentType = MediaType.valueOf("application/zip");
        } else {
            responseBody = new FileStreamingResponseBody(minioService, streamCopier, path);
            contentType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return new DownloadResponse(responseBody, contentType);
    }
}
