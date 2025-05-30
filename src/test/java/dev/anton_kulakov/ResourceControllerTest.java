package dev.anton_kulakov;

import dev.anton_kulakov.config.WithMockCustomUser;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ResourceControllerTest extends BaseIntegrationTest {
    @Autowired
    private MockMvc mvc;

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void uploadFile_shouldReturnStatus201() {
        String paramName = "object";
        String fileName = "test-file.txt";
        String fileNameContent = "Text from test-file.txt";
        String uploadRequestPath = "/api/resource";

        MockMultipartFile testFile = new MockMultipartFile(
                paramName,
                fileName,
                MediaType.TEXT_PLAIN_VALUE,
                fileNameContent.getBytes()
        );

        mvc.perform(multipart(uploadRequestPath)
                        .file(testFile)
                        .param("path", ""))
                .andExpect(status().isCreated());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void uploadFolder_shouldReturnStatus201() {
        String paramName = "object";
        String uploadRequestPath = "/api/resource";
        String mainFolderFileName = "main-folder-file.txt";
        String mainFolderFileContent = "Text from main-folder-file.txt";
        String nestedFolderFileName = "nested-folder-file.txt";
        String nestedFolderFileContent = "Text from nested-folder-file.txt";
        String mainFolderToUpload = "main_folder/";
        String nestedFolderToUpload = "main_folder/nested_folder/";

        MockMultipartFile mainFolderFile = new MockMultipartFile(
                paramName,
                mainFolderToUpload + mainFolderFileName,
                MediaType.TEXT_PLAIN_VALUE,
                mainFolderFileContent.getBytes()
        );

        MockMultipartFile nestedFolderFile = new MockMultipartFile(
                paramName,
                nestedFolderToUpload + nestedFolderFileName,
                MediaType.TEXT_PLAIN_VALUE,
                nestedFolderFileContent.getBytes()
        );

        mvc.perform(multipart(uploadRequestPath)
                        .file(mainFolderFile)
                        .file(nestedFolderFile)
                        .param("path", mainFolderToUpload))
                .andExpect(status().isCreated());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void upload_withEmptyRequestBody_shouldReturnStatus400() {
        String uploadRequestPath = "/api/resource";

        mvc.perform(multipart(uploadRequestPath)
                        .param("path", ""))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void upload_duplicateFileToSamePath_shouldReturnStatus409() {
        String paramName = "object";
        String fileName = "test-file.txt";
        String fileNameContent = "Text from test-file.txt";
        String uploadRequestPath = "/api/resource";

        MockMultipartFile testFile = new MockMultipartFile(
                paramName,
                fileName,
                MediaType.TEXT_PLAIN_VALUE,
                fileNameContent.getBytes()
        );

        mvc.perform(multipart(uploadRequestPath)
                        .file(testFile)
                        .param("path", ""))
                .andExpect(status().isCreated());

        mvc.perform(multipart(uploadRequestPath)
                        .file(testFile)
                        .param("path", ""))
                .andExpect(status().isConflict());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void upload_duplicateFolderToSamePath_shouldReturnStatus409() {
        String paramName = "object";
        String uploadRequestPath = "/api/resource";
        String mainFolderFileName = "main-folder-file.txt";
        String mainFolderFileContent = "Text from main-folder-file.txt";
        String nestedFolderFileName = "nested-folder-file.txt";
        String nestedFolderContent = "Text from nested-folder-file.txt";
        String mainFolderToUpload = "main_folder/";
        String nestedFolderToUpload = "main_folder/nested_folder/";

        MockMultipartFile mainFolderFile = new MockMultipartFile(
                paramName,
                mainFolderToUpload + mainFolderFileName,
                MediaType.TEXT_PLAIN_VALUE,
                mainFolderFileContent.getBytes()
        );

        MockMultipartFile nestedFolderFile = new MockMultipartFile(
                paramName,
                nestedFolderToUpload + nestedFolderFileName,
                MediaType.TEXT_PLAIN_VALUE,
                nestedFolderContent.getBytes()
        );

        mvc.perform(multipart(uploadRequestPath)
                        .file(mainFolderFile)
                        .file(nestedFolderFile)
                        .param("path", mainFolderToUpload))
                .andExpect(status().isCreated());

        mvc.perform(multipart(uploadRequestPath)
                        .file(mainFolderFile)
                        .file(nestedFolderFile)
                        .param("path", mainFolderToUpload))
                .andExpect(status().isConflict());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void getFileInfo_shouldReturnStatus200() {
        String paramName = "object";
        String fileName = "test-file.txt";
        String fileNameContent = "Text from test-file.txt";
        String uploadRequestPath = "/api/resource";
        String getInfoUrl = "/api/resource?path=test-file.txt";

        MockMultipartFile testFile = new MockMultipartFile(
                paramName,
                fileName,
                MediaType.TEXT_PLAIN_VALUE,
                fileNameContent.getBytes()
        );

        mvc.perform(multipart(uploadRequestPath)
                .file(testFile)
                .param("path", ""));

        mvc.perform(get(getInfoUrl))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void getFolderInfo_shouldReturnStatus200() {
        String paramName = "object";
        String uploadRequestPath = "/api/resource";
        String mainFolderFileName = "main-folder-file.txt";
        String mainFolderFileContent = "Text from main-folder-file.txt";
        String nestedFolderFileName = "nested-folder-file.txt";
        String nestedFolderContent = "Text from nested-folder-file.txt";
        String mainFolderToUpload = "main_folder/";
        String nestedFolderToUpload = "/main_folder/nested_folder/";
        String getInfoUrl = "/api/resource?path=" + nestedFolderToUpload;

        MockMultipartFile mainFolderFile = new MockMultipartFile(
                paramName,
                mainFolderToUpload + mainFolderFileName,
                MediaType.TEXT_PLAIN_VALUE,
                mainFolderFileContent.getBytes()
        );

        MockMultipartFile nestedFolderFile = new MockMultipartFile(
                paramName,
                nestedFolderToUpload + nestedFolderFileName,
                MediaType.TEXT_PLAIN_VALUE,
                nestedFolderContent.getBytes()
        );

        mvc.perform(multipart(uploadRequestPath)
                .file(mainFolderFile)
                .file(nestedFolderFile)
                .param("path", mainFolderToUpload));

        mvc.perform(get(getInfoUrl))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void getFileInfo_withInvalidPathParam_shouldReturnStatus400() {
        String paramName = "object";
        String fileName = "test-file.txt";
        String fileNameContent = "Text from test-file.txt";
        String uploadRequestPath = "/api/resource";
        String getInfoUrl = "/api/resource?path=%^^&test-file.txt";

        MockMultipartFile testFile = new MockMultipartFile(
                paramName,
                fileName,
                MediaType.TEXT_PLAIN_VALUE,
                fileNameContent.getBytes()
        );

        mvc.perform(multipart(uploadRequestPath)
                .file(testFile)
                .param("path", ""));

        mvc.perform(get(getInfoUrl))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void getFolderInfo_withInvalidPathParam_shouldReturnStatus400() {
        String paramName = "object";
        String uploadRequestPath = "/api/resource";
        String mainFolderFileName = "main-folder-file.txt";
        String mainFolderFileContent = "Text from main-folder-file.txt";
        String nestedFolderFileName = "nested-folder-file.txt";
        String nestedFolderContent = "Text from nested-folder-file.txt";
        String mainFolderToUpload = "main_folder/";
        String nestedFolderToUpload = "/main_folder/nested_folder/";
        String getInfoUrl = "/api/resource?path=/" + mainFolderToUpload + "^^$))";

        MockMultipartFile mainFolderFile = new MockMultipartFile(
                paramName,
                mainFolderToUpload + mainFolderFileName,
                MediaType.TEXT_PLAIN_VALUE,
                mainFolderFileContent.getBytes()
        );

        MockMultipartFile nestedFolderFile = new MockMultipartFile(
                paramName,
                nestedFolderToUpload + nestedFolderFileName,
                MediaType.TEXT_PLAIN_VALUE,
                nestedFolderContent.getBytes()
        );

        mvc.perform(multipart(uploadRequestPath)
                .file(mainFolderFile)
                .file(nestedFolderFile)
                .param("path", mainFolderToUpload));

        mvc.perform(get(getInfoUrl))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void getFileInfo_whenFileDoesNotExist_shouldReturnStatus404() {
        mvc.perform(get("/api/resource?path=test-file.txt"))
                .andExpect(status().isNotFound());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void getFolderInfo_whenFolderDoesNotExist_shouldReturnStatus404() {
        mvc.perform(get("/api/resource?path=non_existent_folder/"))
                .andExpect(status().isNotFound());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void deleteFile_shouldReturnStatus204() {
        String paramName = "object";
        String fileName = "test-file.txt";
        String fileNameContent = "Text from test-file.txt";
        String requestPath = "/api/resource";
        String userRootFolder = "user-1-files/";

        MockMultipartFile testFile = new MockMultipartFile(
                paramName,
                fileName,
                MediaType.TEXT_PLAIN_VALUE,
                fileNameContent.getBytes()
        );

        mvc.perform(multipart(requestPath)
                .file(testFile)
                .param("path", ""));

        mvc.perform(MockMvcRequestBuilders.delete(requestPath)
                        .param("path", userRootFolder + fileName))
                .andExpect(status().isNoContent());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void deleteFolder_shouldReturnStatus204() {
        String paramName = "object";
        String uploadRequestPath = "/api/resource";
        String mainFolderFileName = "main-folder-file.txt";
        String mainFolderFileContent = "Text from main-folder-file.txt";
        String nestedFolderFileName = "nested-folder-file.txt";
        String nestedFolderFileContent = "Text from nested-folder-file.txt";
        String mainFolderToUpload = "main_folder/";
        String nestedFolderToUpload = "main_folder/nested_folder/";
        String userRootFolder = "user-1-files/";

        MockMultipartFile mainFolderFile = new MockMultipartFile(
                paramName,
                mainFolderToUpload + mainFolderFileName,
                MediaType.TEXT_PLAIN_VALUE,
                mainFolderFileContent.getBytes()
        );

        MockMultipartFile nestedFolderFile = new MockMultipartFile(
                paramName,
                nestedFolderToUpload + nestedFolderFileName,
                MediaType.TEXT_PLAIN_VALUE,
                nestedFolderFileContent.getBytes()
        );

        mvc.perform(multipart(uploadRequestPath)
                .file(mainFolderFile)
                .file(nestedFolderFile)
                .param("path", mainFolderToUpload));

        mvc.perform(MockMvcRequestBuilders.delete(uploadRequestPath)
                        .param("path", userRootFolder + mainFolderToUpload))
                .andExpect(status().isNoContent());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void deleteFile_withEmptyPath_shouldReturnStatus400() {
        String paramName = "object";
        String fileName = "test-file.txt";
        String fileNameContent = "Text from test-file.txt";
        String requestPath = "/api/resource";

        MockMultipartFile testFile = new MockMultipartFile(
                paramName,
                fileName,
                MediaType.TEXT_PLAIN_VALUE,
                fileNameContent.getBytes()
        );

        mvc.perform(multipart(requestPath)
                .file(testFile)
                .param("path", ""));

        mvc.perform(MockMvcRequestBuilders.delete(requestPath))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void deleteFile_withInvalidPath_shouldReturnStatus400() {
        String paramName = "object";
        String fileName = "test-file.txt";
        String fileNameContent = "Text from test-file.txt";
        String requestPath = "/api/resource";
        String userRootFolder = "user-1-files/";

        MockMultipartFile testFile = new MockMultipartFile(
                paramName,
                fileName,
                MediaType.TEXT_PLAIN_VALUE,
                fileNameContent.getBytes()
        );

        mvc.perform(multipart(requestPath)
                .file(testFile)
                .param("path", ""));

        mvc.perform(MockMvcRequestBuilders.delete(requestPath)
                        .param("path", userRootFolder + fileName + "%$##&"))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void deleteFolder_withEmptyPath_shouldReturnStatus400() {
        String paramName = "object";
        String uploadRequestPath = "/api/resource";
        String mainFolderFileName = "main-folder-file.txt";
        String mainFolderFileContent = "Text from main-folder-file.txt";
        String nestedFolderFileName = "nested-folder-file.txt";
        String nestedFolderFileContent = "Text from nested-folder-file.txt";
        String mainFolderToUpload = "main_folder/";
        String nestedFolderToUpload = "main_folder/nested_folder/";

        MockMultipartFile mainFolderFile = new MockMultipartFile(
                paramName,
                mainFolderToUpload + mainFolderFileName,
                MediaType.TEXT_PLAIN_VALUE,
                mainFolderFileContent.getBytes()
        );

        MockMultipartFile nestedFolderFile = new MockMultipartFile(
                paramName,
                nestedFolderToUpload + nestedFolderFileName,
                MediaType.TEXT_PLAIN_VALUE,
                nestedFolderFileContent.getBytes()
        );

        mvc.perform(multipart(uploadRequestPath)
                .file(mainFolderFile)
                .file(nestedFolderFile)
                .param("path", mainFolderToUpload));

        mvc.perform(MockMvcRequestBuilders.delete(uploadRequestPath))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void deleteFolder_withInvalidPath_shouldReturnStatus204() {
        String paramName = "object";
        String uploadRequestPath = "/api/resource";
        String mainFolderFileName = "main-folder-file.txt";
        String mainFolderFileContent = "Text from main-folder-file.txt";
        String nestedFolderFileName = "nested-folder-file.txt";
        String nestedFolderFileContent = "Text from nested-folder-file.txt";
        String mainFolderToUpload = "main_folder/";
        String nestedFolderToUpload = "main_folder/nested_folder/";
        String userRootFolder = "user-1-files/";

        MockMultipartFile mainFolderFile = new MockMultipartFile(
                paramName,
                mainFolderToUpload + mainFolderFileName,
                MediaType.TEXT_PLAIN_VALUE,
                mainFolderFileContent.getBytes()
        );

        MockMultipartFile nestedFolderFile = new MockMultipartFile(
                paramName,
                nestedFolderToUpload + nestedFolderFileName,
                MediaType.TEXT_PLAIN_VALUE,
                nestedFolderFileContent.getBytes()
        );

        mvc.perform(multipart(uploadRequestPath)
                .file(mainFolderFile)
                .file(nestedFolderFile)
                .param("path", mainFolderToUpload));

        mvc.perform(MockMvcRequestBuilders.delete(uploadRequestPath)
                        .param("path", userRootFolder + "%$##&" + mainFolderToUpload))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void deleteFile_whenFileDoesNotExist_shouldReturnStatus404() {
        String requestPath = "/api/resource";
        String userRootFolder = "user-1-files/";
        String fileName = "test-file.txt";

        mvc.perform(MockMvcRequestBuilders.delete(requestPath)
                        .param("path", userRootFolder + fileName))
                .andExpect(status().isNotFound());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void deleteFolder_whenFolderDoesNotExist_shouldReturnStatus204() {
        String uploadRequestPath = "/api/resource";
        String userRootFolder = "user-1-files/";
        String mainFolderToUpload = "main_folder/";

        mvc.perform(MockMvcRequestBuilders.delete(uploadRequestPath)
                        .param("path", userRootFolder + mainFolderToUpload))
                .andExpect(status().isNotFound());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void downloadFile_shouldReturnStatus200() {
        String paramName = "object";
        String fileName = System.currentTimeMillis() + "-test-file.txt";
        String fileContent = "Text from " + fileName;
        String uploadRequestPath = "/api/resource";
        String downloadEndpoint = "/api/resource/download";
        String downloadPath = "user-1-files/" + fileName;

        MockMultipartFile testFile = new MockMultipartFile(
                paramName,
                fileName,
                MediaType.TEXT_PLAIN_VALUE,
                fileContent.getBytes()
        );

        mvc.perform(multipart(uploadRequestPath)
                .file(testFile)
                .param("path", ""));

        mvc.perform(get(downloadEndpoint)
                        .param("path", downloadPath))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void downloadFolder_shouldReturnStatus200() {
        String paramName = "object";
        String uploadRequestPath = "/api/resource";
        String mainFolderFileName = System.currentTimeMillis() + "-main-folder-file.txt";
        String mainFolderFileContent = "Text from " + mainFolderFileName;
        String nestedFolderFileName = System.currentTimeMillis() + "-nested-folder-file.txt";
        String nestedFolderFileContent = "Text from " + nestedFolderFileName;
        String mainFolderToUpload = "main_folder/";
        String nestedFolderToUpload = "main_folder/nested_folder/";
        String downloadEndpoint = "/api/resource/download";
        String downloadPath = "user-1-files/" + mainFolderToUpload;

        MockMultipartFile mainFolderFile = new MockMultipartFile(
                paramName,
                mainFolderToUpload + mainFolderFileName,
                MediaType.TEXT_PLAIN_VALUE,
                mainFolderFileContent.getBytes()
        );

        MockMultipartFile nestedFolderFile = new MockMultipartFile(
                paramName,
                nestedFolderToUpload + nestedFolderFileName,
                MediaType.TEXT_PLAIN_VALUE,
                nestedFolderFileContent.getBytes()
        );

        mvc.perform(multipart(uploadRequestPath)
                .file(mainFolderFile)
                .file(nestedFolderFile)
                .param("path", mainFolderToUpload));

        mvc.perform(get(downloadEndpoint)
                        .param("path", downloadPath))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/zip"));
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void downloadEmptyFolder_ShouldReturnStatus200() {
        String createEmptyFolderEndpoint = "/api/directory";
        String downloadEndpoint = "/api/resource/download";
        String emptyFolderName = "new_empty_folder/";
        String downloadPath = "user-1-files/" + emptyFolderName;

        mvc.perform(post(createEmptyFolderEndpoint)
                .param("path", emptyFolderName));

        mvc.perform(get(downloadEndpoint)
                        .param("path", downloadPath))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/zip"));
    }


    @SneakyThrows
    @Test
    @WithMockCustomUser
    void downloadFile_withEmptyPath_shouldReturnStatus400() {
        String paramName = "object";
        String fileName = System.currentTimeMillis() + "-test-file.txt";
        String fileContent = "Text from " + fileName;
        String uploadRequestPath = "/api/resource";
        String downloadEndpoint = "/api/resource/download";

        MockMultipartFile testFile = new MockMultipartFile(
                paramName,
                fileName,
                MediaType.TEXT_PLAIN_VALUE,
                fileContent.getBytes()
        );

        mvc.perform(multipart(uploadRequestPath)
                .file(testFile)
                .param("path", ""));

        mvc.perform(get(downloadEndpoint))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void downloadFile_withInvalidPath_shouldReturnStatus400() {
        String paramName = "object";
        String fileName = System.currentTimeMillis() + "-test-file.txt";
        String fileContent = "Text from " + fileName;
        String uploadRequestPath = "/api/resource";
        String downloadEndpoint = "/api/resource/download";
        String downloadPath = "user-1-files/" + fileName + "%%^&^))";

        MockMultipartFile testFile = new MockMultipartFile(
                paramName,
                fileName,
                MediaType.TEXT_PLAIN_VALUE,
                fileContent.getBytes()
        );

        mvc.perform(multipart(uploadRequestPath)
                .file(testFile)
                .param("path", ""));

        mvc.perform(get(downloadEndpoint)
                        .param("path", downloadPath))
                .andExpect(status().isBadRequest());
    }


    // скачивание: отсутствующий путь папки кидает ошибку и код 400
    @SneakyThrows
    @Test
    @WithMockCustomUser
    void downloadFolder_withEmptyPath_shouldReturnStatus400() {
        String paramName = "object";
        String uploadRequestPath = "/api/resource";
        String mainFolderFileName = System.currentTimeMillis() + "-main-folder-file.txt";
        String mainFolderFileContent = "Text from " + mainFolderFileName;
        String nestedFolderFileName = System.currentTimeMillis() + "-nested-folder-file.txt";
        String nestedFolderFileContent = "Text from " + nestedFolderFileName;
        String mainFolderToUpload = "main_folder/";
        String nestedFolderToUpload = "main_folder/nested_folder/";
        String downloadEndpoint = "/api/resource/download";

        MockMultipartFile mainFolderFile = new MockMultipartFile(
                paramName,
                mainFolderToUpload + mainFolderFileName,
                MediaType.TEXT_PLAIN_VALUE,
                mainFolderFileContent.getBytes()
        );

        MockMultipartFile nestedFolderFile = new MockMultipartFile(
                paramName,
                nestedFolderToUpload + nestedFolderFileName,
                MediaType.TEXT_PLAIN_VALUE,
                nestedFolderFileContent.getBytes()
        );

        mvc.perform(multipart(uploadRequestPath)
                .file(mainFolderFile)
                .file(nestedFolderFile)
                .param("path", mainFolderToUpload));

        mvc.perform(get(downloadEndpoint))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void downloadFolder_withInvalidPath_shouldReturnStatus400() {
        String paramName = "object";
        String uploadRequestPath = "/api/resource";
        String mainFolderFileName = System.currentTimeMillis() + "-main-folder-file.txt";
        String mainFolderFileContent = "Text from " + mainFolderFileName;
        String nestedFolderFileName = System.currentTimeMillis() + "-nested-folder-file.txt";
        String nestedFolderFileContent = "Text from " + nestedFolderFileName;
        String mainFolderToUpload = "main_folder/";
        String nestedFolderToUpload = "main_folder/nested_folder/";
        String downloadEndpoint = "/api/resource/download";
        String downloadPath = "user-1-files/" + "%%^&^))" + mainFolderToUpload;

        MockMultipartFile mainFolderFile = new MockMultipartFile(
                paramName,
                mainFolderToUpload + mainFolderFileName,
                MediaType.TEXT_PLAIN_VALUE,
                mainFolderFileContent.getBytes()
        );

        MockMultipartFile nestedFolderFile = new MockMultipartFile(
                paramName,
                nestedFolderToUpload + nestedFolderFileName,
                MediaType.TEXT_PLAIN_VALUE,
                nestedFolderFileContent.getBytes()
        );

        mvc.perform(multipart(uploadRequestPath)
                .file(mainFolderFile)
                .file(nestedFolderFile)
                .param("path", mainFolderToUpload));

        mvc.perform(get(downloadEndpoint)
                        .param("path", downloadPath))
                .andExpect(status().isBadRequest());
    }

    // move rename: файла приводит к коду 200 ок
    // move rename: папки приводит к коду 200 ок

    // move rename: невалидный путь файла кидает ошибку и код 400
    // move rename: отсутствующий путь файла кидает ошибку и код 400

    // move rename: невалидный путь папки кидает ошибку и код 400
    // move rename: отсутствующий путь папки кидает ошибку и код 400

    // move rename: отсутствие файла кидает ошибку и код 404
    // move rename: отсутствие папки кидает ошибку и код 404

    // move rename: наличие уже файла кидает ошибку и код 409
    // move rename: наличие уже папки кидает ошибку и код 409

    // поиск: файла дает код 200 ок
    // поиск: папки дает код 200 ок

    // поиск: невалидный запрос на файл кидает ошибку и код 400
    // поиск: отсутствующий запрос на файл кидает ошибку и код 400

    // поиск: невалидный запрос на папку кидает ошибку и код 400
    // поиск: отсутствующий запрос на папку кидает ошибку и код 400
}
