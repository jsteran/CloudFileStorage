package dev.anton_kulakov.service;

import dev.anton_kulakov.dto.DownloadResponse;
import dev.anton_kulakov.exception.ResourceNotFoundException;
import dev.anton_kulakov.streaming.FileStreamingResponseBody;
import dev.anton_kulakov.streaming.FolderStreamingResponseBody;
import dev.anton_kulakov.streaming.StreamCopier;
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
    private final ResourceServiceFactory resourceServiceFactory;
    private final FolderResourceService folderResourceHandler;
    private final PathProcessor pathProcessor;
    private final StreamCopier streamCopier;

    public DownloadResponse prepareDownloadResponse(String path) {
        if (!resourceServiceFactory.getResourceHandler(path).isExists(path)) {
            log.warn("Attempt to download a non-existent resource: {}", path);
            throw new ResourceNotFoundException("The requested resource could not be found");
        }

        StreamingResponseBody responseBody;
        MediaType contentType;

        if (path.endsWith("/")) {
            List<String> resourcesInFolder = folderResourceHandler.getResourcesNamesInFolder(path);
            responseBody = new FolderStreamingResponseBody(minioService, pathProcessor, streamCopier, resourcesInFolder, path);
            contentType = MediaType.valueOf("application/zip");
        } else {
            responseBody = new FileStreamingResponseBody(minioService, streamCopier, path);
            contentType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return new DownloadResponse(responseBody, contentType);
    }
}
