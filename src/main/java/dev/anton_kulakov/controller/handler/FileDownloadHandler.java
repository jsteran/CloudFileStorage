package dev.anton_kulakov.controller.handler;

import dev.anton_kulakov.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Service
@RequiredArgsConstructor
public class FileDownloadHandler implements ResourceDownloadHandler {
    private final MinioService minioService;

    @Override
    public ResponseEntity<StreamingResponseBody> download(String path) {
        FileStreamingResponseBody responseBody = new FileStreamingResponseBody(minioService, path);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(responseBody);
    }
}
