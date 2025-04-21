package dev.anton_kulakov.service;

import dev.anton_kulakov.dto.ResourceInfoDto;
import dev.anton_kulakov.exception.MinioException;
import dev.anton_kulakov.exception.ResourceNotFoundException;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FolderService implements ResourceServiceInterface {
    private final FileService fileService;
    private final MinioClient minioClient;
    private final MinioHelper minioHelper;
    private final PathHelper pathHelper;
    private static final String BUCKET_NAME = "user-files";

    @Override
    public ResourceInfoDto getInfo(String path) {
        if (!isExists(path)) {
            throw new ResourceNotFoundException("The folder with the path %s could not be found".formatted(path));
        }

        return minioHelper.convertToFolderDto(path);
    }

    public boolean isExists(String path) {
        return minioClient.listObjects(ListObjectsArgs.builder()
                        .bucket(BUCKET_NAME)
                        .prefix(path)
                        .maxKeys(1)
                        .recursive(false)
                        .build())
                .iterator().hasNext();
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
        List<String> resourcesNamesInFolder = getResourcesNamesInFolder(from);

        for (String resource : resourcesNamesInFolder) {
            String relativePath = pathHelper.getRelativePath(from, resource);

            try {
                minioClient.copyObject(CopyObjectArgs.builder()
                        .bucket(BUCKET_NAME)
                        .object(to + relativePath)
                        .source(CopySource.builder()
                                .bucket(BUCKET_NAME)
                                .object(resource)
                                .build())
                        .build());
            } catch (Exception e) {
                throw new MinioException("The MinIO service is currently unavailable. Please check the service status and try again later");
            }
        }

        for (String resource : resourcesNamesInFolder) {
            fileService.delete(resource);
        }
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

    public List<ResourceInfoDto> getContent(String path) {
        Iterable<Result<Item>> allResources = getAllResourcesFromUserFolder(path);
        List<ResourceInfoDto> resourcesFound = new ArrayList<>();

        for (Result<Item> resource : allResources) {
            try {
                String resourceName = resource.get().objectName();
                ResourceInfoDto resourceInfoDto;

                if (resourceName.endsWith("/") && resource.get().isDir()) {
                    resourceInfoDto = minioHelper.convertToFolderDto(resource.get().objectName());
                    resourcesFound.add(resourceInfoDto);
                } else if (!resourceName.endsWith("/")) {
                    resourceInfoDto = minioHelper.convertToFileDto(resource.get());
                    resourcesFound.add(resourceInfoDto);
                }

            } catch (Exception e) {
                throw new MinioException("The MinIO service is currently unavailable. Please check the service status and try again later");
            }
        }

        return resourcesFound;
    }

    private Iterable<Result<Item>> getAllResourcesFromUserFolder(String path) {
        return minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(BUCKET_NAME)
                .prefix(path)
                .recursive(false)
                .build());
    }

    public void create(String path) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(path)
                    .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                    .build());
        } catch (Exception e) {
            throw new MinioException("The MinIO service is currently unavailable. Please check the service status and try again later");
        }
    }
}
