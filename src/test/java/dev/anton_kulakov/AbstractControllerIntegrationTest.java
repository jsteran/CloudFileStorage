package dev.anton_kulakov;

import dev.anton_kulakov.service.MinioService;
import dev.anton_kulakov.util.PathProcessor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

public class AbstractControllerIntegrationTest extends BaseIntegrationTest {
    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected MinioService minioService;

    @Autowired
    PathProcessor pathProcessor;

    protected MockMultipartFile createFile(String fileName, String folderToUpload) {
        String paramName = "object";
        String fullFileName = folderToUpload + fileName;
        String fileContent = "Text from " + fullFileName;

        return new MockMultipartFile(
                paramName,
                fullFileName,
                MediaType.TEXT_PLAIN_VALUE,
                fileContent.getBytes()
        );
    }

    @SneakyThrows
    protected void uploadFile(MockMultipartFile file, String path) {
        mvc.perform(MockMvcRequestBuilders.multipart("/api/resource")
                .file(file)
                .param("path", path));
    }
}
