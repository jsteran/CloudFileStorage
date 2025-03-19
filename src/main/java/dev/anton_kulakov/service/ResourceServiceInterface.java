package dev.anton_kulakov.service;

import dev.anton_kulakov.dto.ResourceInfoDto;

public interface ResourceServiceInterface {
    ResourceInfoDto getInfo(String resourceName);

    void delete(String resourceName);

    void move(String from, String to);
}
