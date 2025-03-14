package dev.anton_kulakov.service;

import dev.anton_kulakov.dto.ResourceInfoDto;
import dev.anton_kulakov.exception.MinioException;
import dev.anton_kulakov.exception.ResourceNotFoundException;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class MinioService {

    @Value("${minio.bucket-name}")
    private String bucketName;

    private final MinioClient minioClient;
    private final static String DIRECTORY_TYPE = "DIRECTORY";
    private final static String FILE_TYPE = "FILE";

    public ResourceInfoDto getResourceInfoDto(String resourceName) {
        ResourceInfoDto resourceInfoDto;

        if (resourceName.endsWith("/")) {
            resourceInfoDto = getDirectoryInfoDto(resourceName);
        } else {
            resourceInfoDto = getFileInfoDto(resourceName);
        }

        return resourceInfoDto;
    }

    public void deleteResource(String resourceName) {
        if (resourceName.endsWith("/")) {
            deleteDirectory(resourceName);
        } else {
            deleteFile(resourceName);
        }
    }

    public void streamFile(String fileName, Consumer<InputStream> streamConsumer) {
        try (InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(fileName)
                .build())) {
            streamConsumer.accept(inputStream);
        } catch (Exception e) {
            throw new MinioException("The MinIO service is currently unavailable. Please check the service status and try again later");
        }
    }

    public List<String> getObjectsNamesInFolder(String folderPath) {
        ArrayList<String> objectsNames = new ArrayList<>();

        Iterable<Result<Item>> objectsInFolder = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(folderPath)
                .recursive(true)
                .build());


        for (Result<Item> object : objectsInFolder) {
            try {
                Item item = object.get();

                if (!item.isDir()) {
                    objectsNames.add(item.objectName());
                }
            } catch (Exception e) {
                throw new MinioException("The MinIO service is currently unavailable. Please check the service status and try again later");
            }
        }

        return objectsNames;
    }

    private void deleteDirectory(String fullDirectoryName) {
        List<DeleteObject> objects = getObjectsToDelete(fullDirectoryName);

        if (!objects.isEmpty()) {
            Iterable<Result<DeleteError>> deleteResults = minioClient.removeObjects(RemoveObjectsArgs.builder()
                    .bucket(bucketName)
                    .objects(objects)
                    .build());

            for (Result<DeleteError> result : deleteResults) {
                try {
                    DeleteError error = result.get();
                    System.err.println("Failed to delete: " + error.objectName() + " - " + error.message());
                } catch (Exception e) {
                    throw new MinioException("The MinIO service is currently unavailable. Please check the service status and try again later");
                }
            }
        } else {
            throw new ResourceNotFoundException("Folder is empty or does not exist");
        }
    }

    private List<DeleteObject> getObjectsToDelete(String fullDirectoryName) {
        try {
            List<DeleteObject> objects = new LinkedList<>();
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(fullDirectoryName)
                            .recursive(true)
                            .build()
            );

            for (Result<Item> result : results) {
                Item item = result.get();
                objects.add(new DeleteObject(item.objectName()));
            }
            return objects;

        } catch (Exception e) {
            throw new MinioException("The MinIO service is currently unavailable. Please check the service status and try again later");
        }
    }

    private void deleteFile(String fileName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build());
        } catch (Exception e) {
            throw new MinioException("The MinIO service is currently unavailable. Please check the service status and try again later");
        }
    }


    private ResourceInfoDto getDirectoryInfoDto(String resourceName) {
        try {
            ResourceInfoDto directoryInfoDto = new ResourceInfoDto();
            directoryInfoDto.setPath(resourceName);
            directoryInfoDto.setName(getLastFolderName(resourceName));
            directoryInfoDto.setType(DIRECTORY_TYPE);

            return directoryInfoDto;
        } catch (Exception e) {
            throw new MinioException("The MinIO service is currently unavailable. Please check the service status and try again later");
        }
    }

    private String getLastFolderName(String resourceName) {
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

    private ResourceInfoDto getFileInfoDto(String resourceName) {
        try {
            StatObjectResponse statObjectResponse = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(resourceName)
                    .build());

            ResourceInfoDto resourceInfoDto = new ResourceInfoDto();
            resourceInfoDto.setPath(statObjectResponse.object());
            resourceInfoDto.setName(Paths.get(statObjectResponse.object()).getFileName().toString());
            resourceInfoDto.setSize(statObjectResponse.size());
            resourceInfoDto.setType(FILE_TYPE);

            return resourceInfoDto;
        } catch (Exception e) {
            throw new MinioException("The MinIO service is currently unavailable. Please check the service status and try again later");
        }
    }
}
