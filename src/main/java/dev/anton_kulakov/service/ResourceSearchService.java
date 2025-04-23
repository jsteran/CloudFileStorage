package dev.anton_kulakov.service;

import dev.anton_kulakov.dto.ResourceInfoDto;
import dev.anton_kulakov.mapper.ResourceMapper;
import dev.anton_kulakov.exception.MinioException;
import dev.anton_kulakov.util.PathProcessor;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceSearchService {
    private final PathProcessor pathProcessor;
    private final MinioClient minioClient;
    private final ResourceMapper resourceMapper;
    private static final String BUCKET_NAME = "user-files";

    public List<ResourceInfoDto> search(String userRootFolder, String query) {
        List<String> foldersToSearch = new ArrayList<>();
        List<ResourceInfoDto> resourcesFound = new ArrayList<>();
        foldersToSearch.add(userRootFolder);

        while (!foldersToSearch.isEmpty()) {
            String folderToSearch = foldersToSearch.get(foldersToSearch.size() - 1);
            Iterable<Result<Item>> allResources = getAllResources(folderToSearch);
            foldersToSearch.remove(folderToSearch);

            for (Result<Item> resource : allResources) {
                try {
                    String resourceName = resource.get().objectName();
                    ResourceInfoDto resourceInfoDto;

                    if (resourceName.endsWith("/") && resource.get().isDir()) {
                        resourceInfoDto = resourceMapper.toFolderInfoDto(resource.get().objectName());
                        foldersToSearch.add(resource.get().objectName());

                        if (pathProcessor.getLastFolderName(resource.get().objectName()).toLowerCase().contains(query)) {
                            resourcesFound.add(resourceInfoDto);
                        }

                    } else if (!resourceName.endsWith("/")) {
                        resourceInfoDto = resourceMapper.toFileInfoDto(resource.get());

                        if (pathProcessor.getFileName(resource.get().objectName()).toLowerCase().contains(query)) {
                            resourcesFound.add(resourceInfoDto);
                        }
                    }

                } catch (Exception e) {
                    throw new MinioException("The MinIO service is currently unavailable. Please check the service status and try again later");
                }
            }
        }

        return resourcesFound;
    }

    private Iterable<Result<Item>> getAllResources(String folderName) {
        return minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(BUCKET_NAME)
                .prefix(folderName)
                .recursive(false)
                .build());
    }
}
