package dev.anton_kulakov.mapper;

import dev.anton_kulakov.dto.ResourceInfoDto;
import dev.anton_kulakov.model.ResourceTypeEnum;
import dev.anton_kulakov.util.PathProcessor;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResourceMapper {
    private final PathProcessor pathProcessor;

    public ResourceInfoDto toFileInfoDto(StatObjectResponse statObjectResponse) {
        return createFileInfoDto(statObjectResponse.object(), statObjectResponse.size());
    }

    public ResourceInfoDto toFileInfoDto(Item item) {
        return createFileInfoDto(item.objectName(), item.size());
    }

    public ResourceInfoDto toFolderInfoDto(String fullFolderPath) {
        String pathWithoutRootFolder = pathProcessor.getPathWithoutRootFolder(fullFolderPath);
        ResourceInfoDto resourceInfoDto = new ResourceInfoDto();
        resourceInfoDto.setPath(pathWithoutRootFolder);
        resourceInfoDto.setName(pathProcessor.getLastFolderName(pathWithoutRootFolder));
        resourceInfoDto.setType(ResourceTypeEnum.DIRECTORY);

        return resourceInfoDto;
    }

    private ResourceInfoDto createFileInfoDto(String objectName, long size) {
        ResourceInfoDto resourceInfoDto = new ResourceInfoDto();
        String fileName = pathProcessor.getFileName(objectName);
        String fullFolderPath = pathProcessor.getFolderPath(objectName, fileName);
        String pathWithoutRootFolder = pathProcessor.getPathWithoutRootFolder(fullFolderPath);

        resourceInfoDto.setPath(pathWithoutRootFolder);
        resourceInfoDto.setName(fileName);
        resourceInfoDto.setSize(size);
        resourceInfoDto.setType(ResourceTypeEnum.FILE);

        return resourceInfoDto;
    }
}
