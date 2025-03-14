package dev.anton_kulakov.controller.handler;

import dev.anton_kulakov.exception.MinioException;
import dev.anton_kulakov.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@RequiredArgsConstructor
public class FileStreamingResponseBody implements StreamingResponseBody {
    private final MinioService minioService;
    private final String resourceName;

    @Override
    public void writeTo(@NotNull OutputStream outputStream) {
        minioService.streamFile(resourceName, inputStream -> {
            try {
                copyStream(inputStream, outputStream);
            } catch (IOException e) {
                throw new MinioException("The MinIO service is currently unavailable. Please check the service status and try again later");
            }
        });
    }

    private void copyStream(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
    }
}
