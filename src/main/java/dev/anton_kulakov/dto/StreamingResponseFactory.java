package dev.anton_kulakov.dto;

import dev.anton_kulakov.exception.ResourceNotFoundException;
import dev.anton_kulakov.service.FileService;
import dev.anton_kulakov.service.FolderService;
import dev.anton_kulakov.service.IOHelper;
import dev.anton_kulakov.service.PathHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StreamingResponseFactory {
    private final FileService fileService;
    private final FolderService folderService;
    private final PathHelper pathHelper;
    private final IOHelper ioHelper;

    public StreamingResponseBody createResponse(String path) {
        if (path.endsWith("/")) {
            List<String> resourcesInFolder = folderService.getResourcesNamesInFolder(path);

            if (resourcesInFolder.isEmpty()) {
                throw new ResourceNotFoundException("Folder is empty");
            }

            return new FolderStreamingResponseBody(fileService, pathHelper, ioHelper, resourcesInFolder, path);
        }

        return new FileStreamingResponseBody(fileService, ioHelper, path);
    }

    public MediaType getContentType(String path) {
        return path.endsWith("/") ? MediaType.valueOf("application/zip") : MediaType.APPLICATION_OCTET_STREAM;
    }
}
