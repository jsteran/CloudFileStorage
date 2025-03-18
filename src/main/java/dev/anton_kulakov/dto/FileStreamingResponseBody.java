package dev.anton_kulakov.dto;

import dev.anton_kulakov.service.FileService;
import dev.anton_kulakov.service.IOHelper;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.OutputStream;

@RequiredArgsConstructor
public class FileStreamingResponseBody implements StreamingResponseBody {
    private final FileService fileService;
    private final IOHelper ioHelper;
    private final String resourceName;

    @Override
    public void writeTo(@NotNull OutputStream outputStream) {
        int bufferSize = 1024;

        fileService.streamFile(resourceName, inputStream ->
                ioHelper.copyStream(inputStream, outputStream, bufferSize));
    }
}
