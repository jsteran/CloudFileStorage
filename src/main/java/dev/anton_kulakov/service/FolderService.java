package dev.anton_kulakov.service;

import dev.anton_kulakov.dto.ResourceInfoDto;
import dev.anton_kulakov.exception.MinioException;
import dev.anton_kulakov.exception.ResourceNotFoundException;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FolderService implements ResourceServiceInterface {
    private final MinioClient minioClient;
    private final MinioHelper minioHelper;
    private static final String BUCKET_NAME = "user-files";

    @Override
    public ResourceInfoDto getInfo(String folderName) {
        return minioHelper.convertToFolderDto(folderName);
    }

    @Override
    public void delete(String path) {
        List<DeleteObject> resourcesInFolder = getResourcesNamesInFolder(path).stream()
                .map(DeleteObject::new)
                .collect(Collectors.toList());

        if (resourcesInFolder.isEmpty()) {
            throw new ResourceNotFoundException("Folder is empty or does not exist");
        }

        Iterable<Result<DeleteError>> deleteResults = minioClient.removeObjects(RemoveObjectsArgs.builder()
                .bucket(BUCKET_NAME)
                .objects(resourcesInFolder)
                .build());

        for (Result<DeleteError> result : deleteResults) {
            try {
                DeleteError error = result.get();
                System.err.println("Failed to delete: " + error.objectName() + " - " + error.message());
            } catch (Exception e) {
                throw new MinioException("The MinIO service is currently unavailable. Please check the service status and try again later");
            }
        }
    }

    @Override
    public void move(String from, String to) {

    }

    public List<String> getResourcesNamesInFolder(String folderPath) {
        ArrayList<String> resourcesNames = new ArrayList<>();

        Iterable<Result<Item>> resourcesInFolder = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(BUCKET_NAME)
                .prefix(folderPath)
                .recursive(true)
                .build());

        try {
            for (Result<Item> resource : resourcesInFolder) {
                Item item = resource.get();

                if (!item.isDir()) {
                    resourcesNames.add(item.objectName());
                }
            }

            return resourcesNames;
        } catch (Exception e) {
            throw new MinioException("The MinIO service is currently unavailable. Please check the service status and try again later");
        }
    }
}
