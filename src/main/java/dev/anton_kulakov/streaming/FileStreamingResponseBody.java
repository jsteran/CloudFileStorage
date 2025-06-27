package dev.anton_kulakov.streaming;

import dev.anton_kulakov.exception.BaseAppException;
import dev.anton_kulakov.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Slf4j
@RequiredArgsConstructor
public class FileStreamingResponseBody implements StreamingResponseBody {
    private final MinioService minioService;
    private final StreamCopier streamCopier;
    private final String resourceName;

    @Override
    public void writeTo(@NotNull OutputStream outputStream) {
        try (InputStream inputStream = minioService.getObject(resourceName)) {
            int bufferSize = 1024;
            streamCopier.copyStream(inputStream, outputStream, bufferSize);
        } catch (IOException e) {
            log.error("Failed to stream file", e);
            throw new BaseAppException("Failed to stream file: " + resourceName);
        }
    }
}
