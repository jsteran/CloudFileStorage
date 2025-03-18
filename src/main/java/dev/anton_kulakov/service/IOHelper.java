package dev.anton_kulakov.service;

import dev.anton_kulakov.exception.MinioException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Component
public class IOHelper {
    public void copyStream(InputStream inputStream, OutputStream outputStream, int bufferSize) {
        byte[] buffer = new byte[bufferSize];
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
