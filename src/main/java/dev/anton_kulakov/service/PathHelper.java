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

    public String getFileName(String objectName) {
        String[] splitObjectName;

        try {
            splitObjectName = objectName.split("/");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return splitObjectName[splitObjectName.length - 1];
    }

    public String getFolderPath(String fullPath, String fileName) {

        if (fullPath.endsWith(fileName)) {
            int lastSlashIndex = fullPath.lastIndexOf("/");

            if (lastSlashIndex != -1) {
                return fullPath.substring(0, lastSlashIndex + 1);
            }
        }

        return fullPath;
    }

    public String getUserRootFolder(int userId) {
        return "user-" + userId + "-files/";
    }
}
