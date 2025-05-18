package dev.anton_kulakov;

import io.minio.*;
import io.minio.messages.Item;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public class BaseMinioIntegrationTest {
    @Autowired
    private MinioClient minioClient;

    private static final String MINIO_TEST_BUCKET_NAME = "test-bucket";
    private static final String MINIO_ACCESS_KEY = "minioadmin";
    private static final String MINIO_SECRET_KEY = "minioadmin";
    private static final int MINIO_PORT = 9000;

    @Container
    private static GenericContainer<?> minio = new GenericContainer<>(DockerImageName.parse("minio/minio:latest"))
            .withExposedPorts(MINIO_PORT)
            .withEnv("MINIO_ROOT_USER", MINIO_ACCESS_KEY)
            .withEnv("MINIO_ROOT_PASSWORD", MINIO_SECRET_KEY)
            .withCommand("server", "/data")
            .waitingFor(new HttpWaitStrategy()
                    .forPath("/minio/health/live")
                    .forPort(MINIO_PORT))
            .withStartupTimeout(Duration.ofSeconds(30));

    @DynamicPropertySource
    static void minioProperties(DynamicPropertyRegistry registry) {
        if (!minio.isRunning()) {
            minio.start();
        }

        registry.add("minio.url", () -> "http://" + minio.getHost() + ":" + minio.getMappedPort(MINIO_PORT));
        registry.add("minio.accessKey", () -> MINIO_ACCESS_KEY);
        registry.add("minio.secretKey", () -> MINIO_SECRET_KEY);
    }

    @SneakyThrows
    @BeforeAll
    static void setupBucket(@Autowired MinioClient minioClient) {
        boolean isBucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(MINIO_TEST_BUCKET_NAME).build());

        if (!isBucketExists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(MINIO_TEST_BUCKET_NAME).build());
        }
    }

    @SneakyThrows
    @AfterEach
    void cleanupMinio() {
        Iterable<Result<Item>> objects = minioClient.listObjects(ListObjectsArgs.builder().bucket(MINIO_TEST_BUCKET_NAME).recursive(true).build());

        for (Result<Item> object : objects) {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(MINIO_TEST_BUCKET_NAME).object(object.get().objectName()).build());
        }
    }
}
