package dev.anton_kulakov;

import dev.anton_kulakov.service.MinioService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@ImportAutoConfiguration(classes = {RedisAutoConfiguration.class})
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class RegistrationControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private MinioService minioService;

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));

    @Container
    private static final GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:latest"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379).toString());
    }

    @Test
    @DisplayName("Creating a new user results in a new record in the database")
    @SneakyThrows
    void shouldCreateUser() {
        String requestBody = """
                {
                  "username": "test_user1",
                  "password": "test_password"
                }
                """;

        mvc.perform(post("http://localhost:8080/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("test_user1"));
    }

    @SneakyThrows
    @Test
    @DisplayName("A registration request with invalid username will result in an error")
    void shouldThrowExceptionIfUsernameNotValid() {
        String requestBody = """
                {
                  "username": "test",
                  "password": "test_password"
                }
                """;

        mvc.perform(post("http://localhost:8080/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    @DisplayName("A registration request with invalid password will result in an error")
    void shouldThrowExceptionCredentialsAreNotValid() {
        String requestBody = """
                {
                  "username": "test_user1",
                  "password": "pass"
                }
                """;

        mvc.perform(post("http://localhost:8080/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    @DisplayName("Creating a user with a duplicate username results in an error")
    void shouldThrowExceptionIfUserAlreadyExists() {
        String requestBody = """
                {
                  "username": "test_user1",
                  "password": "test_password"
                }
                """;

        mvc.perform(post("http://localhost:8080/api/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        mvc.perform(post("http://localhost:8080/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict());
    }
}
