package dev.anton_kulakov.service;

import dev.anton_kulakov.exception.MinioException;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${minio.bucket-name}")
    private String bucketName;

    @PostConstruct
    private void createBucketIfNotExists() {
        try {
            boolean isBucketExists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());

            if (!isBucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
            }
        } catch (Exception e) {
            throw new MinioException("There is an issue when verifying the existence of the bucket");
        }

    }

    public StatObjectResponse getStatObject(String path) {
        try {
            return minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(path)
                    .build());
        } catch (Exception e) {
            throw new MinioException("Failed to retrieve object metadata");
        }
    }

    public List<Item> getListObjects(String path, boolean isRecursive) {
        List<Item> results = new ArrayList<>();

        Iterable<Result<Item>> objects = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(path)
                .recursive(isRecursive)
                .build());

        for (Result<Item> object : objects) {
            try {
                results.add(object.get());
            } catch (Exception e) {
                throw new MinioException("Failed to list objects");
            }
        }

        return results;
    }

    public void removeObject(String path) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(path)
                    .build());
        } catch (Exception e) {
            throw new MinioException("Failed to remove object");
        }
    }

    public void removeObjects(List<DeleteObject> objects) {
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(RemoveObjectsArgs.builder()
                .bucket(bucketName)
                .objects(objects)
                .build());

        for (Result<DeleteError> result : results) {
            try {
                DeleteError error = result.get();
                System.err.println("Failed to delete: " + error.objectName() + " - " + error.message());
            } catch (Exception e) {
                throw new MinioException("Failed to process batch deletion result");
            }
        }
    }

    public void copy(String from, String to) {
        try {
            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(bucketName)
                    .object(to)
                    .source(CopySource.builder()
                            .bucket(bucketName)
                            .object(from)
                            .build())
                    .build());
        } catch (Exception e) {
            removeObject(to);
            throw new MinioException("Failed to copy object");
        }
    }

    public void streamFile(String fileName, Consumer<InputStream> streamConsumer) {
        try (InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(fileName)
                .build())) {
            streamConsumer.accept(inputStream);
        } catch (Exception e) {
            throw new MinioException("Failed to stream file");
        }
    }

    public void upload(String path, MultipartFile file) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(path + file.getOriginalFilename())
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
        } catch (Exception e) {
            throw new MinioException("Failed to upload file");
        }
    }

    public boolean isFileExists(String path) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(path)
                    .build());
            return true;
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code()) ||
                "NoSuchObject".equals(e.errorResponse().code())) {
                return false;
            }

            throw new MinioException("Error checking existence of file");
        } catch (Exception e) {
            throw new MinioException("Failed to check if file exists");
        }
    }

    public boolean isFolderExists(String path) {
        return minioClient.listObjects(ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(path)
                        .maxKeys(1)
                        .recursive(false)
                        .build())
                .iterator().hasNext();
    }

    public void createEmptyFolder(String path) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(path)
                    .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                    .build());
        } catch (Exception e) {
            throw new MinioException("Failed to create empty folder");
        }
    }
}
