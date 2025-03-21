package dev.anton_kulakov.service;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class PathHelper {
    public String getLastFolderName(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            return "";
        }

        List<String> clearedOfEmptyPartsName = Arrays.stream(resourcePath.split("/"))
                .filter(s -> !s.isBlank())
                .toList();

        if (clearedOfEmptyPartsName.isEmpty()) {
            return "";
        }

        return clearedOfEmptyPartsName.get(clearedOfEmptyPartsName.size() - 1) + "/";
    }

    public String getRelativePath(String pathWithoutResourceName, String fullResourcePath) {
        return fullResourcePath.substring(pathWithoutResourceName.length());
    }
}
