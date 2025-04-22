package dev.anton_kulakov.streaming;

import dev.anton_kulakov.exception.MinioException;
import dev.anton_kulakov.service.FileService;
import dev.anton_kulakov.service.IOHelper;
import dev.anton_kulakov.service.PathHelper;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RequiredArgsConstructor
public class FolderStreamingResponseBody implements StreamingResponseBody {
    private final FileService fileService;
    private final PathHelper pathHelper;
    private final IOHelper ioHelper;
    private final List<String> resourcesInFolder;
    private final String pathWithoutResourceName;

    @Override
    public void writeTo(@NotNull OutputStream outputStream) {
        try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {

            for (String resource : resourcesInFolder) {
                addResourceToZip(zipOut, resource);
            }
        } catch (IOException e) {
            throw new MinioException("The MinIO service is currently unavailable. Please check the service status and try again later");
        }
    }

    private void addResourceToZip(ZipOutputStream zipOut, String fullResourceName) throws IOException {
        String entryName = pathHelper.getRelativePath(pathWithoutResourceName, fullResourceName);
        int bufferSize = 1024;

        try {
            zipOut.putNextEntry(new ZipEntry(entryName));
            fileService.streamFile(fullResourceName, inputStream -> ioHelper.copyStream(inputStream, zipOut, bufferSize));
        } catch (IOException e) {
            throw new MinioException("The MinIO service is currently unavailable. Please check the service status and try again later");
        }

        zipOut.closeEntry();
    }
}
