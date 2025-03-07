package dev.anton_kulakov.service;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;


}
