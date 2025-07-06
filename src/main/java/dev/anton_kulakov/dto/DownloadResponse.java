package dev.anton_kulakov.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DownloadResponse {
    private StreamingResponseBody responseBody;
    private MediaType contentType;
}
