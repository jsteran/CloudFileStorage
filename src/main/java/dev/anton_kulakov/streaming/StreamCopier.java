package dev.anton_kulakov.streaming;

import dev.anton_kulakov.exception.BaseAppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Slf4j
@Component
public class StreamCopier {
    public void copyStream(InputStream inputStream, OutputStream outputStream, int bufferSize) {
        int endOfStream = -1;
        byte[] buffer = new byte[bufferSize];
        int bytesRead;

        try {
            while ((bytesRead = inputStream.read(buffer)) != endOfStream) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            log.error("Stream copy failed. An I/O error occurred", e);
            throw new BaseAppException("Failed to copy stream data: I/O error during read/write operation");
        }
    }
}
