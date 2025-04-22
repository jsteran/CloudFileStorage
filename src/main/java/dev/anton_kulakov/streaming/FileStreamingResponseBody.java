package dev.anton_kulakov.streaming;

import dev.anton_kulakov.service.FileService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.OutputStream;

@RequiredArgsConstructor
public class FileStreamingResponseBody implements StreamingResponseBody {
    private final FileService fileService;
    private final StreamCopier streamCopier;
    private final String resourceName;

    @Override
    public void writeTo(@NotNull OutputStream outputStream) {
        int bufferSize = 1024;

        fileService.streamFile(resourceName, inputStream ->
                streamCopier.copyStream(inputStream, outputStream, bufferSize));
    }
}
