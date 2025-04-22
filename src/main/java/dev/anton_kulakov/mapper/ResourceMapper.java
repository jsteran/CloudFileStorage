package dev.anton_kulakov.mapper;

import dev.anton_kulakov.dto.ResourceInfoDto;
import dev.anton_kulakov.model.ResourceTypeEnum;
import dev.anton_kulakov.service.PathHelper;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResourceMapper {
    private final PathHelper pathHelper;

    public ResourceInfoDto toFileInfoDto(StatObjectResponse statObjectResponse) {
        return createFileInfoDto(statObjectResponse.object(), statObjectResponse.size());
    }

    public ResourceInfoDto toFileInfoDto(Item item) {
        return createFileInfoDto(item.objectName(), item.size());
    }

    public ResourceInfoDto toFolderInfoDto(String folderPath) {
        ResourceInfoDto resourceInfoDto = new ResourceInfoDto();
        resourceInfoDto.setPath(folderPath);
        resourceInfoDto.setName(pathHelper.getLastFolderName(folderPath));
        resourceInfoDto.setType(ResourceTypeEnum.DIRECTORY);

        return resourceInfoDto;
    }

    private ResourceInfoDto createFileInfoDto(String objectName, long size) {
        ResourceInfoDto resourceInfoDto = new ResourceInfoDto();
        String fileName = pathHelper.getFileName(objectName);
        String folderPath = pathHelper.getFolderPath(objectName, fileName);

        resourceInfoDto.setPath(folderPath);
        resourceInfoDto.setName(fileName);
        resourceInfoDto.setSize(size);
        resourceInfoDto.setType(ResourceTypeEnum.FILE);

        return resourceInfoDto;
    }
}
