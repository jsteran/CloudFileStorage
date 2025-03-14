package dev.anton_kulakov.controller.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public interface ResourceDownloadHandler {
    ResponseEntity<StreamingResponseBody> download(String path);
}
