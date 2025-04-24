package dev.anton_kulakov.streaming;

import dev.anton_kulakov.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.OutputStream;

@RequiredArgsConstructor
public class FileStreamingResponseBody implements StreamingResponseBody {
    private final MinioService minioService;
    private final StreamCopier streamCopier;
    private final String resourceName;

    @Override
    public void writeTo(@NotNull OutputStream outputStream) {
        int bufferSize = 1024;

        minioService.streamFile(resourceName, inputStream ->
                streamCopier.copyStream(inputStream, outputStream, bufferSize));
    }
}
