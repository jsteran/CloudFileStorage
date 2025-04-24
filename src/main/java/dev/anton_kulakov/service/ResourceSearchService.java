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
public class ResourceSearchService {
    private final PathProcessor pathProcessor;
    private final MinioService minioService;
    private final ResourceMapper resourceMapper;

    public List<ResourceInfoDto> search(String userRootFolder, String query) {
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
                    addIfMatchesQuery(query, objectName, resourcesFound, resourceInfoDto);
                } else if (!objectName.endsWith("/")) {
                    resourceInfoDto = resourceMapper.toFileInfoDto(object);
                    addIfMatchesQuery(query, objectName, resourcesFound, resourceInfoDto);
                }
            }
        }

        return resourcesFound;
    }

    private void addIfMatchesQuery(String query, String objectName, List<ResourceInfoDto> resourcesFound, ResourceInfoDto resourceInfoDto) {
        String lowerCaseFolderName = pathProcessor.getLastFolderName(objectName).toLowerCase();

        if (lowerCaseFolderName.contains(query)) {
            resourcesFound.add(resourceInfoDto);
        }
    }
}
