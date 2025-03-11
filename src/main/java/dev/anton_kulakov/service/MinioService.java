package dev.anton_kulakov.service;

import dev.anton_kulakov.dto.ResourceInfoDto;
import dev.anton_kulakov.exception.MinioException;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MinioService {

    @Value("${minio.bucket-name}")
    private String bucketName;

    private final MinioClient minioClient;

    public ResourceInfoDto getResourceInfoDto(String name) {
        ResourceInfoDto resourceInfoDto;

        if (name.endsWith("/")) {
            resourceInfoDto = getDirectoryInfoDto(name);
        } else {
            resourceInfoDto = getFileInfoDto(name);
        }

        return resourceInfoDto;
    }

    private ResourceInfoDto getDirectoryInfoDto(String name) {
        try {
            ResourceInfoDto directoryInfoDto = new ResourceInfoDto();
            directoryInfoDto.setPath(name);
            directoryInfoDto.setName(getLastFolderName(name));
            directoryInfoDto.setType("DIRECTORY");

            return directoryInfoDto;
        } catch (Exception e) {
            throw new MinioException("The MinIO service is currently unavailable. Please check the service status and try again later");
        }
    }

    private String getLastFolderName(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }

        List<String> clearedOfEmptyPartsName = Arrays.stream(name.split("/"))
                .filter(s -> !s.isBlank())
                .toList();

        if (clearedOfEmptyPartsName.isEmpty()) {
            return "";
        }

        return clearedOfEmptyPartsName.get(clearedOfEmptyPartsName.size() - 1) + "/";
    }

    private ResourceInfoDto getFileInfoDto(String name) {
        try {
            StatObjectResponse statObjectResponse = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(name)
                    .build());

            ResourceInfoDto resourceInfoDto = new ResourceInfoDto();
            resourceInfoDto.setPath(statObjectResponse.object());
            resourceInfoDto.setName(Paths.get(statObjectResponse.object()).getFileName().toString());
            resourceInfoDto.setSize(statObjectResponse.size());
            resourceInfoDto.setType("FILE");

            return resourceInfoDto;
        } catch (Exception e) {
            throw new MinioException("The MinIO service is currently unavailable. Please check the service status and try again later");
        }
    }
}
