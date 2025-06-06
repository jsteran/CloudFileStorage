package dev.anton_kulakov;

import dev.anton_kulakov.exception.MinioException;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BaseIntegrationTest {
    private static final String MINIO_ACCESS_KEY = "minioadmin";
    private static final String MINIO_SECRET_KEY = "minioadmin";

    private static final String TEST_BUCKET_NAME_FOR_DYNAMIC_PROPERTIES = "test-bucket-name";

    @Value("${minio.bucket-name}")
    private String injectedBucketName;

    @Autowired
    private MinioClient minioClient;

    protected static final PostgreSQLContainer<?> postgres;
    protected static final GenericContainer<?> minio;

    static {
        postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));
        postgres.start();

        minio = new GenericContainer<>("minio/minio")
                .withEnv("MINIO_ACCESS_KEY", MINIO_ACCESS_KEY)
                .withEnv("MINIO_SECRET_KEY", MINIO_SECRET_KEY)
                .withCommand("server /data")
                .withExposedPorts(9000)
                .waitingFor(new HttpWaitStrategy()
                        .forPath("/minio/health/ready")
                        .forPort(9000)
                        .withStartupTimeout(Duration.ofSeconds(10)));

        minio.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (minio.isRunning()) minio.stop();
            if (postgres.isRunning()) postgres.stop();
        }));
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("minio.endpoint", () -> String.format("http://%s:%s", minio.getHost(), minio.getFirstMappedPort()));
        registry.add("minio.access-key", () -> MINIO_ACCESS_KEY);
        registry.add("minio.secret-key", () -> MINIO_SECRET_KEY);
        registry.add("minio.bucket-name", () -> TEST_BUCKET_NAME_FOR_DYNAMIC_PROPERTIES);
    }

    @BeforeEach
    void cleanUpFileStorage() {
        try {
            boolean isExists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(injectedBucketName)
                    .build());

            if (!isExists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(injectedBucketName)
                        .build());
            }
        } catch (Exception e) {
            throw new MinioException("Failed to ensure bucket '" + injectedBucketName + "' exists for cleanup. " + e);
        }

        List<String> resourcesNames = new ArrayList<>();

        Iterable<Result<Item>> resources = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(injectedBucketName)
                .recursive(true)
                .build());

        for (Result<Item> resource : resources) {
            try {
                resourcesNames.add(resource.get().objectName());
            } catch (Exception e) {
                throw new MinioException("Failed to list objects during cleanup. Bucket: " + injectedBucketName + " " + e);
            }
        }

        if (!resourcesNames.isEmpty()) {
            List<DeleteObject> resourcesToDelete = resourcesNames.stream()
                    .map(DeleteObject::new)
                    .collect(Collectors.toList());

            Iterable<Result<DeleteError>> results = minioClient.removeObjects(RemoveObjectsArgs.builder()
                    .bucket(injectedBucketName)
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
    }
}
