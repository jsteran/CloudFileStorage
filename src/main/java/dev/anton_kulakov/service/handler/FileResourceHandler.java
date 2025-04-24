package dev.anton_kulakov.service.handler;

import dev.anton_kulakov.dto.ResourceInfoDto;
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
        return resourceMapper.toFileInfoDto(minioService.getStatObject(path));
    }

    @Override
    public void delete(String path) {
        minioService.removeObject(path);
    }

    @Override
    public void move(String from, String to) {
        minioService.copy(from, to);
        minioService.removeObject(from);
    }

    @Override
    public boolean isExists(String path) {
        return minioService.isFileExists(path);
    }

    @Override
    public ResourceInfoDto upload(String path, MultipartFile file) {
        minioService.upload(path, file);
        String newPath = path + file.getOriginalFilename();
        return getInfo(newPath);
    }
}
