package dev.anton_kulakov.service;

import dev.anton_kulakov.dto.ResourceInfoDto;
import dev.anton_kulakov.exception.ResourceAlreadyExistsException;
import dev.anton_kulakov.exception.ResourceNotFoundException;
import dev.anton_kulakov.mapper.ResourceMapper;
import dev.anton_kulakov.util.PathProcessor;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class FolderResourceService implements ResourceServiceInterface {
    private final MinioService minioService;
    private final ResourceMapper resourceMapper;
    private final PathProcessor pathProcessor;

    @Override
    public ResourceInfoDto getInfo(String path) {
        if (!minioService.isFolderExists(path)) {
            log.error("The folder with path {} does not exist", path);
            throw new ResourceNotFoundException("The requested folder could not be found");
        }

        return resourceMapper.toFolderInfoDto(path);
    }

    @Override
    public void delete(String path) {
        List<DeleteObject> resourcesInFolder = getResourcesNamesInFolder(path).stream()
                .map(DeleteObject::new)
                .collect(Collectors.toList());

        if (resourcesInFolder.isEmpty()) {
            log.error("The folder with path {} is empty or does not exist", path);
            throw new ResourceNotFoundException("Folder is empty or does not exist");
        }

        minioService.removeObjects(resourcesInFolder);
    }

    @Override
    public String move(String from, String to) {
        if (minioService.isFolderExists(to)) {
            log.error("The folder with path {} is already exists", to);
            throw new ResourceAlreadyExistsException("The folder already exists at the destination path: %s".formatted(to));
        }

        List<String> resourcesNamesInFolder = getResourcesNamesInFolder(from);

        for (String resource : resourcesNamesInFolder) {
            String relativePath = pathProcessor.getRelativePath(from, resource);
            minioService.copy(resource, to + relativePath);
        }

        for (String resource : resourcesNamesInFolder) {
            minioService.removeObject(resource);
        }

        return to;
    }

    @Override
    public boolean isExists(String path) {
        return minioService.isFolderExists(path);
    }

    public List<String> getResourcesNamesInFolder(String path) {
        ArrayList<String> resourcesNames = new ArrayList<>();
        List<Item> objects = minioService.getListObjects(path, true);

        for (Item object : objects) {
            if (!object.isDir()) {
                resourcesNames.add(object.objectName());
            }
        }

        return resourcesNames;
    }

    public List<ResourceInfoDto> getContent(String path) {
        List<Item> objects = minioService.getListObjects(path, false);

        if (objects.isEmpty() && !minioService.isFolderExists(path)) {
            log.warn("Attempted to get content of a non-existent folder: {}", path);
            throw new ResourceNotFoundException("The folder with the path %s could not be found".formatted(path));
        }

        List<ResourceInfoDto> resources = new ArrayList<>();

        for (Item object : objects) {
            String resourceName = object.objectName();
            ResourceInfoDto resourceInfoDto;

            if (resourceName.endsWith("/") && object.isDir()) {
                resourceInfoDto = resourceMapper.toFolderInfoDto(object.objectName());
                resources.add(resourceInfoDto);
            } else if (!resourceName.endsWith("/")) {
                resourceInfoDto = resourceMapper.toFileInfoDto(object);
                resources.add(resourceInfoDto);
            }
        }

        return resources;
    }

    public ResourceInfoDto create(String path, int userId) {
        String userRootFolder = pathProcessor.getUserRootFolder(userId);
        String newFolderName = pathProcessor.getLastFolderName(path);
        String parentFolderPath = pathProcessor.getPathWithoutLastFolder(path, newFolderName);

        if (!parentFolderPath.equals(userRootFolder) && !minioService.isFolderExists(parentFolderPath)) {
            log.error("Attempt to create folder in a non-existent parent folder: {}", parentFolderPath);
            throw new ResourceNotFoundException("The parent folder doesn't exist");
        }

        minioService.createEmptyFolder(path, true);
        return resourceMapper.toFolderInfoDto(path);
    }
}
