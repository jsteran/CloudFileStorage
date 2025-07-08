package dev.anton_kulakov.service;

import dev.anton_kulakov.dto.ResourceInfoDto;

public interface ResourceServiceInterface {
    ResourceInfoDto getInfo(String path);

    void delete(String path);

    String move(String from, String to);

    boolean isExists(String path);
}
