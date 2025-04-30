package dev.anton_kulakov.service.handler;

import dev.anton_kulakov.dto.ResourceInfoDto;
import dev.anton_kulakov.exception.ResourceAlreadyExistsException;
import dev.anton_kulakov.exception.ResourceNotFoundException;
import dev.anton_kulakov.mapper.ResourceMapper;
import dev.anton_kulakov.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class FileResourceHandler implements ResourceHandlerInterface {
    private final MinioService minioService;
    private final ResourceMapper resourceMapper;

    @Override
    public ResourceInfoDto getInfo(String path) {
        if (!minioService.isFileExists(path)) {
            throw new ResourceNotFoundException("The requested file could not be found");
        }

        return resourceMapper.toFileInfoDto(minioService.getStatObject(path));
    }

    @Override
    public void delete(String path) {
        if (!minioService.isFileExists(path)) {
            throw new ResourceNotFoundException("The requested file could not be found");
        }

        minioService.removeObject(path);
    }

    @Override
    public void move(String from, String to) {
        if (!minioService.isFileExists(from)) {
            throw new ResourceNotFoundException("The requested file could not be found");
        }

        if (minioService.isFileExists(to)) {
            throw new ResourceAlreadyExistsException("The file already exists at the destination path: %s".formatted(to));
        }

        minioService.copy(from, to);
        minioService.removeObject(from);
    }

    @Override
    public boolean isExists(String path) {
        return minioService.isFileExists(path);
    }

    @Override
    public ResourceInfoDto upload(String path, MultipartFile file) {
        if (minioService.isFileExists(path)) {
            throw new ResourceAlreadyExistsException("The file already exists at the destination path: %s".formatted(path));
        }

        minioService.upload(path, file);
        String newPath = path + file.getOriginalFilename();
        return getInfo(newPath);
    }
}
