package dev.anton_kulakov.model;

import lombok.Getter;

@Getter
public enum ResourceTypeEnum {
    FILE("FILE"),
    DIRECTORY("DIRECTORY");

    private final String value;

    ResourceTypeEnum(String value) {
        this.value = value;
    }
}
