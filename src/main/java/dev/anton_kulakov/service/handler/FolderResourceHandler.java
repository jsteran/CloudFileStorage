package dev.anton_kulakov.service.handler;

import dev.anton_kulakov.dto.ResourceInfoDto;
import dev.anton_kulakov.exception.ResourceAlreadyExistsException;
import dev.anton_kulakov.exception.ResourceNotFoundException;
import dev.anton_kulakov.service.FolderService;
import dev.anton_kulakov.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class FolderResourceHandler implements ResourceHandlerInterface {
    private final FolderService folderService;
    private final MinioService minioService;

    @Override
    public ResourceInfoDto getInfo(String path) {
        if (!minioService.isFolderExists(path)) {
            log.error("The folder with path {} does not exist", path);
            throw new ResourceNotFoundException("The requested folder could not be found");
        }

        return folderService.getInfo(path);
    }

    @Override
    public void delete(String path) {
        if (!minioService.isFolderExists(path)) {
            log.error("The folder with path {} does not exist", path);
            throw new ResourceNotFoundException("The requested folder could not be found");
        }

        folderService.delete(path);
    }

    @Override
    public String move(String from, String to) {
        if (!minioService.isFolderExists(from)) {
            log.error("The folder with path {} does not exist", from);
            throw new ResourceNotFoundException("The requested folder could not be found");
        }

        if (minioService.isFolderExists(to)) {
            log.error("The folder with path {} is already exists", to);
            throw new ResourceAlreadyExistsException("The folder already exists at the destination path: %s".formatted(to));
        }

        folderService.move(from, to);
        return to;
    }

    @Override
    public boolean isExists(String path) {
        return minioService.isFolderExists(path);
    }

    @Override
    public ResourceInfoDto upload(String path, MultipartFile file) {
        if (minioService.isFolderExists(path)) {
            log.error("The folder with path {} is already exists", path);
            throw new ResourceAlreadyExistsException("The folder already exists at the destination path: %s".formatted(path));
        }

        minioService.upload(path, file);
        String newPath = path + file.getOriginalFilename();
        return getInfo(newPath);
    }
}
