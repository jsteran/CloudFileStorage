package dev.anton_kulakov.controller;

import dev.anton_kulakov.exception.MinioException;
import dev.anton_kulakov.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RequiredArgsConstructor
public class FolderStreamingResponseBody implements StreamingResponseBody {
    private final MinioService minioService;
    private final List<String> objectsInFolder;
    private final String folderPath;
    @Override
    public void writeTo(@NotNull OutputStream outputStream) {
        try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {
            for (String object : objectsInFolder) {
                addObjectToZip(zipOut, object);
            }
        } catch (IOException e) {
            throw new MinioException("The MinIO service is currently unavailable. Please check the service status and try again later");
        }
    }

    private void addObjectToZip(ZipOutputStream zipOut, String object) throws IOException {
        String entryName = getRelativePath(object);

        try {
            zipOut.putNextEntry(new ZipEntry(entryName));

            minioService.streamFile(object, inputStream -> copyStream(inputStream, zipOut));
        } catch (IOException e) {
            throw new MinioException("The MinIO service is currently unavailable. Please check the service status and try again later");
        }

        zipOut.closeEntry();
    }

    private String getRelativePath(String fullPath) {
        return fullPath.substring(folderPath.length());
    }

    private void copyStream(InputStream inputStream, OutputStream outputStream) {
        byte[] buffer = new byte[1024];
        int bytesRead;

        try {
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new MinioException("The MinIO service is currently unavailable. Please check the service status and try again later");
        }
    }
}
