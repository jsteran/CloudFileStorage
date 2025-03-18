package dev.anton_kulakov.service;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class PathHelper {
    public String getLastFolderName(String resourceName) {
        if (resourceName == null || resourceName.isBlank()) {
            return "";
        }

        List<String> clearedOfEmptyPartsName = Arrays.stream(resourceName.split("/"))
                .filter(s -> !s.isBlank())
                .toList();

        if (clearedOfEmptyPartsName.isEmpty()) {
            return "";
        }

        return clearedOfEmptyPartsName.get(clearedOfEmptyPartsName.size() - 1) + "/";
    }

    public String getRelativePath(String pathWithoutResourceName, String fullResourceName) {
        return fullResourceName.substring(pathWithoutResourceName.length());
    }
}
