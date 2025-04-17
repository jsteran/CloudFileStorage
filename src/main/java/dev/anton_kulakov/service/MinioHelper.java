package dev.anton_kulakov.service;

import dev.anton_kulakov.dto.ResourceInfoDto;
import dev.anton_kulakov.dto.ResourceTypeEnum;
import dev.anton_kulakov.exception.MinioException;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MinioHelper {
    private final PathHelper pathHelper;
    private final MinioClient minioClient;
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
                        resourceInfoDto = convertToFolderDto(resource.get().objectName());
                        foldersToSearch.add(resource.get().objectName());

                        if (pathHelper.getLastFolderName(resource.get().objectName()).toLowerCase().contains(query)) {
                            resourcesFound.add(resourceInfoDto);
                        }

                    } else if (!resourceName.endsWith("/")) {
                        resourceInfoDto = convertToFileDto(resource.get());

                        if (pathHelper.getFileName(resource.get().objectName()).toLowerCase().contains(query)) {
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

    public ResourceInfoDto convertToFileDto(StatObjectResponse statObjectResponse) {
        ResourceInfoDto resourceInfoDto = new ResourceInfoDto();
        String fileName = pathHelper.getFileName(statObjectResponse.object());
        String folderPath = pathHelper.getFolderPath(statObjectResponse.object(), fileName);

        resourceInfoDto.setPath(folderPath);
        resourceInfoDto.setName(fileName);
        resourceInfoDto.setSize(statObjectResponse.size());
        resourceInfoDto.setType(ResourceTypeEnum.FILE);

        return resourceInfoDto;
    }

    public ResourceInfoDto convertToFileDto(Item minioItem) {
        String fileName = pathHelper.getFileName(minioItem.objectName());
        String folderPath = pathHelper.getFolderPath(minioItem.objectName(), fileName);

        ResourceInfoDto resourceInfoDto = new ResourceInfoDto();
        resourceInfoDto.setPath(folderPath);
        resourceInfoDto.setName(fileName);
        resourceInfoDto.setSize(minioItem.size());
        resourceInfoDto.setType(ResourceTypeEnum.FILE);

        return resourceInfoDto;
    }

    public ResourceInfoDto convertToFolderDto(String folderPath) {
        ResourceInfoDto resourceInfoDto = new ResourceInfoDto();
        resourceInfoDto.setPath(folderPath);
        resourceInfoDto.setName(pathHelper.getLastFolderName(folderPath));
        resourceInfoDto.setType(ResourceTypeEnum.DIRECTORY);

        return resourceInfoDto;
    }

    private Iterable<Result<Item>> getAllResources(String folderName) {
        return minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(BUCKET_NAME)
                .prefix(folderName)
                .recursive(false)
                .build());
    }
}
