package dev.anton_kulakov;

import dev.anton_kulakov.config.WithMockCustomUser;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ResourceControllerTest extends BaseIntegrationTest {
    @Autowired
    private MockMvc mvc;

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void uploadFile_shouldAppearInUserRootFolder() {
        MockMultipartFile testFile = new MockMultipartFile(
                "object",
                "test-file.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Text from test-file.txt".getBytes()
        );

        mvc.perform(multipart("/api/resource")
                        .file(testFile)
                        .param("path", ""))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/resource?path=test-file3.txt"))
                .andExpect(status().isOk());
    }
}
