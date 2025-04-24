package dev.anton_kulakov.service;

import dev.anton_kulakov.exception.MinioException;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class MinioService {
    private final MinioClient minioClient;
    private static final String BUCKET_NAME = "user-files";

    public StatObjectResponse getStatObject(String path) {
        try {
            return minioClient.statObject(StatObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(path)
                    .build());
        } catch (Exception e) {
            throw new MinioException("The MinIO service is currently unavailable. Please check the service status and try again later");
        }
    }

    public List<Item> getListObjects(String path, boolean isRecursive) {
        List<Item> results = new ArrayList<>();

        Iterable<Result<Item>> objects = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(BUCKET_NAME)
                .prefix(path)
                .recursive(isRecursive)
                .build());

        for (Result<Item> object : objects) {
            try {
                results.add(object.get());
            } catch (Exception e) {
                throw new MinioException("The MinIO service is currently unavailable. Please check the service status and try again later");
            }
        }

        return results;
    }

    public void removeObject(String path) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(path)
                    .build());
        } catch (Exception e) {
            throw new MinioException("The MinIO service is currently unavailable. Please check the service status and try again later");
        }
    }

    public void removeObjects(List<DeleteObject> objects) {
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(RemoveObjectsArgs.builder()
                .bucket(BUCKET_NAME)
                .objects(objects)
                .build());

        for (Result<DeleteError> result : results) {
            try {
                DeleteError error = result.get();
                System.err.println("Failed to delete: " + error.objectName() + " - " + error.message());
            } catch (Exception e) {
                throw new MinioException("The MinIO service is currently unavailable. Please check the service status and try again later");
            }
        }
    }

    public void copy(String from, String to) {
        try {
            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(to)
                    .source(CopySource.builder()
                            .bucket(BUCKET_NAME)
                            .object(from)
                            .build())
                    .build());
        } catch (Exception e) {
            removeObject(to);
            throw new MinioException("The MinIO service is currently unavailable. Please check the service status and try again later");
        }
    }

    public void streamFile(String fileName, Consumer<InputStream> streamConsumer) {
        try (InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(BUCKET_NAME)
                .object(fileName)
                .build())) {
            streamConsumer.accept(inputStream);
        } catch (Exception e) {
            throw new MinioException("The MinIO service is currently unavailable. Please check the service status and try again later");
        }
    }

    public void upload(String path, MultipartFile file) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(path + file.getOriginalFilename())
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
        } catch (Exception e) {
            throw new MinioException("The MinIO service is currently unavailable. Please check the service status and try again later");
        }
    }

    public boolean isFileExists(String path) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(path)
                    .build());
            return true;
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code()) ||
                "NoSuchObject".equals(e.errorResponse().code())) {
                return false;
            }

            throw new MinioException("Error checking file existence");
        } catch (Exception e) {
            throw new MinioException("The MinIO service is currently unavailable. Please check the service status and try again later");
        }
    }

    public boolean isFolderExists(String path) {
        return minioClient.listObjects(ListObjectsArgs.builder()
                        .bucket(BUCKET_NAME)
                        .prefix(path)
                        .maxKeys(1)
                        .recursive(false)
                        .build())
                .iterator().hasNext();
    }

    public void createEmptyFolder(String path) {
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
