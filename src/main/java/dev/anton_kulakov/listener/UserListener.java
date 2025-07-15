package dev.anton_kulakov.listener;

import dev.anton_kulakov.model.User;
import dev.anton_kulakov.service.MinioService;
import jakarta.persistence.PostPersist;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserListener {
    private final MinioService minioService;

    @PostPersist
    public void postPersist(User user) {
        minioService.createUserRootFolder(user.getId());
    }
}
