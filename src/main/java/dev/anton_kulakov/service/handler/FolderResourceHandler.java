package dev.anton_kulakov.service.handler;

import dev.anton_kulakov.dto.ResourceInfoDto;
import dev.anton_kulakov.service.FolderService;
import dev.anton_kulakov.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class FolderResourceHandler implements ResourceHandlerInterface {
    private final FolderService folderService;
    private final MinioService minioService;

    @Override
    public ResourceInfoDto getInfo(String path) {
        return folderService.getInfo(path);
    }

    @Override
    public void delete(String path) {
        folderService.delete(path);
    }

    @Override
    public void move(String from, String to) {
        folderService.move(from, to);
    }

    @Override
    public boolean isExists(String path) {
        return minioService.isFolderExists(path);
    }

    @Override
    public ResourceInfoDto upload(String path, MultipartFile file) {
        minioService.upload(path, file);
        String newPath = path + file.getOriginalFilename();
        return getInfo(newPath);
    }
}
