package dev.anton_kulakov.service;

import dev.anton_kulakov.dto.ResourceInfoDto;
import dev.anton_kulakov.exception.MinioException;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class FileService implements ResourceServiceInterface {
    private final MinioClient minioClient;
    private final MinioHelper minioHelper;
    private static final String BUCKET_NAME = "user-files";


    @Override
    public ResourceInfoDto getInfo(String fileName) {
        StatObjectResponse resourceInfo = minioHelper.getResourceInfo(fileName);
        return minioHelper.convertToFileDto(resourceInfo);
    }

    @Override
    public void delete(String fileName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(fileName)
                    .build());
        } catch (Exception e) {
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
}
