package dev.anton_kulakov.service;

import dev.anton_kulakov.exception.MinioException;
import dev.anton_kulakov.exception.ResourceAlreadyExistsException;
import dev.anton_kulakov.exception.ResourceNotFoundException;
import dev.anton_kulakov.util.PathProcessor;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {
    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    private final PathProcessor pathProcessor;

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
            log.error("Failed to create or verify bucket '{}'", bucketName, e);
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
            log.error("Failed to retrieve metadata for object in bucket '{}'. Path: '{}'", bucketName, path, e);
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
                log.warn("Could not retrieve one of the items in bucket '{}' with path '{}'. Skipping it.", bucketName, path, e);
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
            log.error("Failed to remove object with path '{}' from bucket '{}'", path, bucketName, e);
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
                log.warn("Failed to delete object '{}' in bucket '{}'. Reason: {}", error.objectName(), error.bucketName(), error.message());
            } catch (Exception e) {
                log.error("A critical error occurred during batch deletion from bucket '{}'", bucketName, e);
                throw new MinioException("Failed to process batch deletion result");
            }
        }
    }

    public void copy(String from, String to) {
        try {
            StatObjectResponse statObjectResponse = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(from)
                    .build());

            String etag = statObjectResponse.etag();

            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(bucketName)
                    .object(to)
                    .source(CopySource.builder()
                            .bucket(bucketName)
                            .object(from)
                            .matchETag(etag)
                            .build())
                    .build());

            log.info("Successfully copied object from '{}' to '{}' in bucket '{}'", from, to, bucketName);
        } catch (ErrorResponseException e) {
            String errorCode = e.errorResponse().code();

            if ("NoSuchKey".equals(errorCode)) {
                log.warn("Attempted to move a non-existent resource: {}", from);
                throw new ResourceNotFoundException("The source file could not be found: " + from);
            }

            if ("PreconditionFailed".equals(errorCode)) {
                log.warn("Optimistic lock failed: resource '{}' was modified during the move operation", from);
                throw new ConcurrentModificationException("Resource '%s' was modified during the move operation. Please try again".formatted(from));
            }
            log.error("MinIO error during conditional copy from '{}' to '{}'", from, to, e);
            throw new MinioException("MinIO error during conditional copy");
        } catch (Exception e) {
            log.error("Unexpected error during conditional copy from '{}' to '{}'", from, to, e);
            throw new MinioException("Unexpected error during conditional copy");
        }
    }

    public InputStream getObject(String resourceName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(resourceName)
                    .build());
        } catch (Exception e) {
            log.error("Failed to get object '{}' from bucket '{}'", resourceName, bucketName, e);
            throw new MinioException("Failed to get input stream");
        }
    }

    public void upload(String fullObjectPath, MultipartFile file, boolean preventOverwrite) {
        try {
            HashMap<String, String> headers = new HashMap<>();
            if (preventOverwrite) {
                headers.put("If-None-Match", "*");
            }

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fullObjectPath)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .headers(headers)
                    .build());
        } catch (ErrorResponseException e) {
            if (preventOverwrite && "PreconditionFailed".equals(e.errorResponse().code())) {
                throw new ResourceAlreadyExistsException("Resource already exists: %s".formatted(fullObjectPath));
            }
            log.error("Upload failed for '{}'", fullObjectPath, e);
            throw new MinioException("Upload failed");
        } catch (Exception e) {
            log.error("Upload failed for '{}'", fullObjectPath, e);
            throw new MinioException("Upload failed");
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
                log.trace("File '{}' does not exist in bucket '{}' (NoSuchKey/NoSuchObject).", path, bucketName);
                return false;
            }

            log.error("An unexpected MinIO error occurred while checking existence of '{}' in bucket '{}'", path, bucketName, e);
            throw new MinioException("Error checking existence of file");
        } catch (Exception e) {
            log.error("A general error occurred while checking existence of '{}' in bucket '{}'", path, bucketName, e);
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

    public void createEmptyFolder(String path, boolean preventOverwrite) {
        try {
            Map<String, String> headers = new HashMap<>();
            if (preventOverwrite) {
                headers.put("If-None-Match", "*");
            }
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(path)
                    .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                    .headers(headers)
                    .build());
        } catch (ErrorResponseException e) {
            if (preventOverwrite && "PreconditionFailed".equals(e.errorResponse().code())) {
                throw new ResourceAlreadyExistsException("Folder already exists: " + path);
            }
            log.error("Create folder failed: '{}'", path, e);
            throw new MinioException("Create folder failed");
        } catch (Exception e) {
            log.error("Create folder failed: '{}'", path, e);
            throw new MinioException("Create folder failed");
        }
    }

    public void createUserRootFolder(int userId) {
        String userRootFolder = pathProcessor.getUserRootFolder(userId);
        try {
            createEmptyFolder(userRootFolder, true);
        } catch (ResourceAlreadyExistsException e) {
            log.info("User folder already exists: {}", userRootFolder);
        }
    }
}
