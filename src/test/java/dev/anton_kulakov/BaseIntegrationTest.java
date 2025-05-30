package dev.anton_kulakov;

import dev.anton_kulakov.exception.MinioException;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BaseIntegrationTest {
    private static final String MINIO_ACCESS_KEY = "minioadmin";
    private static final String MINIO_SECRET_KEY = "minioadmin";

    private static final String testBucketName = "test-bucket";
    private static GenericContainer<?> minio;
    private static String minioUrl;

    @Autowired
    private MinioClient minioClient;

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));

    @BeforeAll
    static void setUp() {
        int port = 9000;

        minio = new GenericContainer<>("minio/minio")
                .withEnv("MINIO_ACCESS_KEY", MINIO_ACCESS_KEY)
                .withEnv("MINIO_SECRET_KEY", MINIO_SECRET_KEY)
                .withCommand("server /data")
                .withExposedPorts(port)
                .waitingFor(new HttpWaitStrategy()
                        .forPath("/minio/health/ready")
                        .forPort(port)
                        .withStartupTimeout(Duration.ofSeconds(10)));

        minio.start();
        Integer mappedPort = minio.getFirstMappedPort();
        org.testcontainers.Testcontainers.exposeHostPorts(mappedPort);
        minioUrl = String.format("http://%s:%s", minio.getHost(), mappedPort);
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("minio.endpoint", () -> minioUrl);
        registry.add("minio.access-key", () -> MINIO_ACCESS_KEY);
        registry.add("minio.secret-key", () -> MINIO_SECRET_KEY);
        registry.add("minio.bucket-name", () -> testBucketName);
    }

    @BeforeEach
    void cleanUpFileStorage() {
        List<String> resourcesNames = new ArrayList<>();

        Iterable<Result<Item>> resources = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(testBucketName)
                .recursive(true)
                .build());

        for (Result<Item> resource : resources) {
            try {
                resourcesNames.add(resource.get().objectName());
            } catch (Exception e) {
                throw new MinioException("Failed to list objects");
            }
        }

        List<DeleteObject> resourcesToDelete = resourcesNames.stream()
                .map(DeleteObject::new)
                .collect(Collectors.toList());

        Iterable<Result<DeleteError>> results = minioClient.removeObjects(RemoveObjectsArgs.builder()
                .bucket(testBucketName)
                .objects(resourcesToDelete)
                .build());

        for (Result<DeleteError> result : results) {
            try {
                DeleteError error = result.get();
                System.err.println("Failed to delete: " + error.objectName() + " - " + error.message());
            } catch (Exception e) {
                throw new MinioException("Failed to process batch deletion result");
            }
        }
    }

    @AfterAll
    static void shutDown() {
        if (minio.isRunning()) {
            minio.stop();
        }
    }
}
