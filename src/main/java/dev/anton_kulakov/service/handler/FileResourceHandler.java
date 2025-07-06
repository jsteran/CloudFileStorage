package dev.anton_kulakov.service.handler;

import dev.anton_kulakov.dto.ResourceInfoDto;
import dev.anton_kulakov.exception.InvalidMoveOperationException;
import dev.anton_kulakov.exception.ResourceAlreadyExistsException;
import dev.anton_kulakov.exception.ResourceNotFoundException;
import dev.anton_kulakov.mapper.ResourceMapper;
import dev.anton_kulakov.service.MinioService;
import dev.anton_kulakov.util.PathProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileResourceHandler implements ResourceHandlerInterface {
    private final MinioService minioService;
    private final ResourceMapper resourceMapper;
    private final PathProcessor pathProcessor;

    @Override
    public ResourceInfoDto getInfo(String path) {
        if (!minioService.isFileExists(path)) {
            log.error("The requested file with path {} could not be found", path);
            throw new ResourceNotFoundException("The requested file could not be found");
        }

        return resourceMapper.toFileInfoDto(minioService.getStatObject(path));
    }

    @Override
    public void delete(String path) {
        if (!minioService.isFileExists(path)) {
            log.error("The requested file with path {} could not be found", path);
            throw new ResourceNotFoundException("The requested file could not be found");
        }

        minioService.removeObject(path);
    }

    @Override
    public void move(String from, String to) {
        if (!isDestinationPathValid(to)) {
            log.error("The destination path {} for file {} is invalid", to, from);
            throw new InvalidMoveOperationException("The destination path for file is invalid");
        }

        if (!minioService.isFileExists(from)) {
            log.error("The requested file with path {} could not be found", from);
            throw new ResourceNotFoundException("The requested file could not be found");
        }

        if (minioService.isFileExists(to)) {
            log.error("The file with path {} is already exists", to);
            throw new ResourceAlreadyExistsException("The file already exists at the destination path: %s".formatted(to));
        }

        String fileExtension = pathProcessor.getFileExtension(from);

        if (!to.contains(fileExtension)) {
            to += fileExtension;
        }

        minioService.copy(from, to);
        minioService.removeObject(from);
    }

    public boolean isDestinationPathValid(String to) {
        return !to.endsWith("/");
    }

    @Override
    public boolean isExists(String path) {
        return minioService.isFileExists(path);
    }

    @Override
    public ResourceInfoDto upload(String path, MultipartFile file) {
        if (minioService.isFileExists(path + file)) {
            log.error("The file with path {} is already exists", path + file);
            throw new ResourceAlreadyExistsException("The file already exists at the destination path: %s".formatted(path));
        }

        minioService.upload(path, file);
        String newPath = path + file.getOriginalFilename();
        return getInfo(newPath);
    }
}
