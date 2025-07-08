package dev.anton_kulakov.service.handler;

import dev.anton_kulakov.dto.ResourceInfoDto;

public interface ResourceHandlerInterface {
    ResourceInfoDto getInfo(String path);

    void delete(String path);

    String move(String from, String to);

    boolean isExists(String path);
}
