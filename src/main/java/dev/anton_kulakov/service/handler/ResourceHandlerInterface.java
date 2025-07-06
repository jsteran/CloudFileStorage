package dev.anton_kulakov.service.handler;

import dev.anton_kulakov.dto.ResourceInfoDto;
import org.springframework.web.multipart.MultipartFile;

public interface ResourceHandlerInterface {
    ResourceInfoDto getInfo(String path);

    void delete(String path);

    String move(String from, String to);

    boolean isExists(String path);

    ResourceInfoDto upload(String path, MultipartFile file);
}
