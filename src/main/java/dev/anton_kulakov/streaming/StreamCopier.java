package dev.anton_kulakov.streaming;

import dev.anton_kulakov.exception.MinioException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
            throw new MinioException("The MinIO service is currently unavailable. Please check the service status and try again later");
        }
    }
}
