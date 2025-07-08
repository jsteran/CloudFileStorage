package dev.anton_kulakov.service;

import dev.anton_kulakov.dto.ResourceInfoDto;
import dev.anton_kulakov.mapper.ResourceMapper;
import dev.anton_kulakov.util.PathProcessor;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final PathProcessor pathProcessor;
    private final MinioService minioService;
    private final ResourceMapper resourceMapper;

    public List<ResourceInfoDto> search(int userId, String query) {
        String userRootFolder = pathProcessor.getUserRootFolder(userId);
        List<String> foldersToSearch = new ArrayList<>();
        List<ResourceInfoDto> resourcesFound = new ArrayList<>();
        foldersToSearch.add(userRootFolder);

        while (!foldersToSearch.isEmpty()) {
            String folderToSearch = foldersToSearch.get(foldersToSearch.size() - 1);
            List<Item> objects = minioService.getListObjects(folderToSearch, false);
            foldersToSearch.remove(folderToSearch);

            for (Item object : objects) {
                ResourceInfoDto resourceInfoDto;
                String objectName = object.objectName();

                if (objectName.endsWith("/") && object.isDir()) {
                    foldersToSearch.add(objectName);
                    resourceInfoDto = resourceMapper.toFolderInfoDto(objectName);
                    addIfMatchesQuery(query, objectName, resourcesFound, resourceInfoDto, userRootFolder);
                } else if (!objectName.endsWith("/")) {
                    resourceInfoDto = resourceMapper.toFileInfoDto(object);
                    addIfMatchesQuery(query, objectName, resourcesFound, resourceInfoDto, userRootFolder);
                }
            }
        }

        return resourcesFound;
    }

    private void addIfMatchesQuery(String query, String objectName, List<ResourceInfoDto> resourcesFound, ResourceInfoDto resourceInfoDto, String userRootFolder) {
        String lowerCaseFolderName = pathProcessor.getLastFolderName(objectName).toLowerCase();

        if (lowerCaseFolderName.contains(query)) {
            String pathWithoutUserRootFolder = resourceInfoDto.getPath().substring(userRootFolder.length());
            resourceInfoDto.setPath(pathWithoutUserRootFolder);
            resourcesFound.add(resourceInfoDto);
        }
    }
}
