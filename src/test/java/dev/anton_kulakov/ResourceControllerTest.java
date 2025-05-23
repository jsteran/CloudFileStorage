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

    // удаление: удаление файла выдает код 204 NO content без тела
    // удаление: удаление папки выдает код 204 NO content без тела

    // удаление: невалидный путь файла кидает ошибку и код 400
    // удаление: отсутствующий путь файла кидает ошибку и код 400

    // удаление: невалидный путь папки кидает ошибку и код 400
    // удаление: отсутствующий путь папки кидает ошибку и код 400

    // удаление: отсутствие файла кидает ошибку и код 404
    // удаление: отсутствие папки кидает ошибку и код 404

    // скачивание: скач файла дает 200 ок и бинарное содержимое
    // скачивание: скач папки дает 200 ок и бинарное содержимое
    // скачивание: скач ПУСТОЙ папки тоже дает 200 ок и пустой zip

    // скачивание: невалидный путь файла кидает ошибку и код 400
    // скачивание: отсутствующий путь файла кидает ошибку и код 400

    // скачивание: невалидный путь папки кидает ошибку и код 400
    // скачивание: отсутствующий путь папки кидает ошибку и код 400

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
