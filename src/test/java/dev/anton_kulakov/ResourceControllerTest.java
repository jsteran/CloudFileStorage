package dev.anton_kulakov;

import dev.anton_kulakov.config.WithMockCustomUser;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ResourceControllerTest extends AbstractControllerIntegrationTest {
    @SneakyThrows
    @Test
    @WithMockCustomUser
    void uploadFile_shouldReturnStatus201() {
        String fileName = System.currentTimeMillis() + "-test-file.txt";
        String uploadRequestPath = "/api/resource";
        MockMultipartFile file = createFile(fileName, "");

        mvc.perform(multipart(uploadRequestPath)
                        .file(file)
                        .param("path", ""))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", instanceOf(List.class)))
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].path", is("")))
                .andExpect(jsonPath("$[0].name", is(fileName)))
                .andExpect(jsonPath("$[0].size", is(file.getBytes().length)))
                .andExpect(jsonPath("$[0].type", is("FILE")));
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void uploadFolder_shouldReturnStatus201() {
        String mainFolderFileName = System.currentTimeMillis() + "-main-folder-file.txt";
        String nestedFolderFileName = System.currentTimeMillis() + "-nested-folder-file.txt";
        MockMultipartFile mainFolderFile = createFile(mainFolderFileName, "main_folder/");
        MockMultipartFile nestedFolderFile = createFile(nestedFolderFileName, "main_folder/nested_folder/");

        mvc.perform(multipart("/api/resource")
                        .file(mainFolderFile)
                        .file(nestedFolderFile)
                        .param("path", ""))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", instanceOf(List.class)))
                .andExpect(jsonPath("$.length()", is(2)))

                .andExpect(jsonPath("$[0].path", is("main_folder/")))
                .andExpect(jsonPath("$[0].name", is(mainFolderFileName)))
                .andExpect(jsonPath("$[0].size", is(mainFolderFile.getBytes().length)))
                .andExpect(jsonPath("$[0].type", is("FILE")))

                .andExpect(jsonPath("$[1].path", is("main_folder/nested_folder/")))
                .andExpect(jsonPath("$[1].name", is(nestedFolderFileName)))
                .andExpect(jsonPath("$[1].size", is(nestedFolderFile.getBytes().length)))
                .andExpect(jsonPath("$[1].type", is("FILE")));
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void upload_withEmptyRequestBody_shouldReturnStatus400() {
        mvc.perform(multipart("/api/resource")
                        .param("path", ""))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void upload_duplicateFileToSamePath_shouldReturnStatus409() {
        MockMultipartFile testFile = createFile(System.currentTimeMillis() + "test-file.txt", "");
        uploadFile(testFile, "");

        mvc.perform(multipart("/api/resource")
                        .file(testFile)
                        .param("path", ""))
                .andExpect(status().isConflict());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void upload_duplicateFolderToSamePath_shouldReturnStatus409() {
        MockMultipartFile mainFolderFile = createFile(System.currentTimeMillis() + "-main-folder-file.txt", "main_folder/");
        MockMultipartFile nestedFolderFile = createFile(System.currentTimeMillis() + "-nested-folder-file.txt", "main_folder/nested_folder/");
        uploadFile(mainFolderFile, "");
        uploadFile(nestedFolderFile, "main_folder/");

        mvc.perform(multipart("/api/resource")
                        .file(mainFolderFile)
                        .file(nestedFolderFile)
                        .param("path", "main_folder/"))
                .andExpect(status().isConflict());
    }

    @SneakyThrows
    @Test
    void uploadFile_withUnauthorizedUser_shouldReturnStatus401() {
        String fileName = System.currentTimeMillis() + "-test-file.txt";
        String uploadRequestPath = "/api/resource";
        MockMultipartFile file = createFile(fileName, "");

        mvc.perform(multipart(uploadRequestPath)
                        .file(file)
                        .param("path", ""))
                .andExpect(status().isUnauthorized());
    }

    @SneakyThrows
    @Test
    void uploadFolder_withUnauthorizedUser_shouldReturnStatus401() {
        String mainFolderFileName = System.currentTimeMillis() + "-main-folder-file.txt";
        String nestedFolderFileName = System.currentTimeMillis() + "-nested-folder-file.txt";
        MockMultipartFile mainFolderFile = createFile(mainFolderFileName, "main_folder/");
        MockMultipartFile nestedFolderFile = createFile(nestedFolderFileName, "main_folder/nested_folder/");

        mvc.perform(multipart("/api/resource")
                        .file(mainFolderFile)
                        .file(nestedFolderFile)
                        .param("path", ""))
                .andExpect(status().isUnauthorized());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void getFileInfo_shouldReturnStatus200() {
        String fileName = System.currentTimeMillis() + "-test-file.txt";
        MockMultipartFile file = createFile(fileName, "");
        uploadFile(file, "");

        mvc.perform(get("/api/resource?path=" + fileName))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.path", is("")))
                .andExpect(jsonPath("$.name", is(fileName)))
                .andExpect(jsonPath("$.size", is(file.getBytes().length)))
                .andExpect(jsonPath("$.type", is("FILE")));
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void getFolderInfo_shouldReturnStatus200() {
        MockMultipartFile mainFolderFile = createFile(System.currentTimeMillis() + "-main-folder-file.txt", "main_folder/");
        MockMultipartFile nestedFolderFile = createFile(System.currentTimeMillis() + "-nested-folder-file.txt", "nested_folder/");
        uploadFile(mainFolderFile, "");
        uploadFile(nestedFolderFile, "main_folder/");

        mvc.perform(get("/api/resource?path=main_folder/nested_folder/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.path", is("main_folder/nested_folder/")))
                .andExpect(jsonPath("$.name", is("nested_folder/")))
                .andExpect(jsonPath("$.type", is("DIRECTORY")));
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void getFileInfo_withInvalidPathParam_shouldReturnStatus400() {
        String fileName = System.currentTimeMillis() + "-test-file.txt";
        MockMultipartFile file = createFile(fileName, "");
        uploadFile(file, "");

        mvc.perform(get("/api/resource?path=%^^&test-file.txt"))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void getFolderInfo_withInvalidPathParam_shouldReturnStatus400() {
        MockMultipartFile mainFolderFile = createFile(System.currentTimeMillis() + "-main-folder-file.txt", "main_folder/");
        MockMultipartFile nestedFolderFile = createFile(System.currentTimeMillis() + "-nested-folder-file.txt", "nested_folder/");
        uploadFile(mainFolderFile, "");
        uploadFile(nestedFolderFile, "main_folder/");

        mvc.perform(get("/api/resource?path=/main_folder/^^$))"))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    void getFileInfo_withUnauthorizedUser_shouldReturnStatus401() {
        String fileName = System.currentTimeMillis() + "-test-file.txt";
        MockMultipartFile file = createFile(fileName, "");
        uploadFile(file, "");

        mvc.perform(get("/api/resource?path=%^^&test-file.txt"))
                .andExpect(status().isUnauthorized());
    }

    @SneakyThrows
    @Test
    void getFolderInfo_withUnauthorizedUser_shouldReturnStatus401() {
        MockMultipartFile mainFolderFile = createFile(System.currentTimeMillis() + "-main-folder-file.txt", "main_folder/");
        MockMultipartFile nestedFolderFile = createFile(System.currentTimeMillis() + "-nested-folder-file.txt", "nested_folder/");
        uploadFile(mainFolderFile, "");
        uploadFile(nestedFolderFile, "main_folder/");

        mvc.perform(get("/api/resource?path=main_folder/"))
                .andExpect(status().isUnauthorized());
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
        String fileName = System.currentTimeMillis() + "-test-file.txt";
        MockMultipartFile file = createFile(fileName, "");
        uploadFile(file, "");

        mvc.perform(MockMvcRequestBuilders.delete("/api/resource")
                        .param("path", fileName))
                .andExpect(status().isNoContent());
    }


    @SneakyThrows
    @Test
    @WithMockCustomUser
    void deleteFolder_shouldReturnStatus204() {
        MockMultipartFile mainFolderFile = createFile(System.currentTimeMillis() + "-main-folder-file.txt", "main_folder/");
        MockMultipartFile nestedFolderFile = createFile(System.currentTimeMillis() + "-nested-folder-file.txt", "nested_folder/");
        uploadFile(mainFolderFile, "");
        uploadFile(nestedFolderFile, "main_folder/");

        mvc.perform(MockMvcRequestBuilders.delete("/api/resource")
                        .param("path", "main_folder/"))
                .andExpect(status().isNoContent());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void deleteFile_withEmptyPath_shouldReturnStatus400() {
        String fileName = System.currentTimeMillis() + "-test-file.txt";
        MockMultipartFile file = createFile(fileName, "");
        uploadFile(file, "");

        mvc.perform(MockMvcRequestBuilders.delete("/api/resource"))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void deleteFile_withInvalidPath_shouldReturnStatus400() {
        String fileName = System.currentTimeMillis() + "-test-file.txt";
        MockMultipartFile file = createFile(fileName, "");
        uploadFile(file, "");

        mvc.perform(MockMvcRequestBuilders.delete("/api/resource")
                        .param("path", fileName + "%$##&"))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void deleteFolder_withEmptyPath_shouldReturnStatus400() {
        MockMultipartFile mainFolderFile = createFile(System.currentTimeMillis() + "-main-folder-file.txt", "main_folder/");
        MockMultipartFile nestedFolderFile = createFile(System.currentTimeMillis() + "-nested-folder-file.txt", "nested_folder/");
        uploadFile(mainFolderFile, "");
        uploadFile(nestedFolderFile, "main_folder/");

        mvc.perform(MockMvcRequestBuilders.delete("/api/resource"))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void deleteFolder_withInvalidPath_shouldReturnStatus400() {
        MockMultipartFile mainFolderFile = createFile(System.currentTimeMillis() + "-main-folder-file.txt", "main_folder/");
        MockMultipartFile nestedFolderFile = createFile(System.currentTimeMillis() + "-nested-folder-file.txt", "nested_folder/");
        uploadFile(mainFolderFile, "");
        uploadFile(nestedFolderFile, "main_folder/");

        mvc.perform(MockMvcRequestBuilders.delete("/api/resource")
                        .param("path", "%$##&main_folder/"))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    void deleteFile_withUnauthorizedUser_shouldReturnStatus401() {
        String fileName = System.currentTimeMillis() + "-test-file.txt";
        MockMultipartFile file = createFile(fileName, "");
        uploadFile(file, "");

        mvc.perform(MockMvcRequestBuilders.delete("/api/resource")
                        .param("path", fileName))
                .andExpect(status().isUnauthorized());
    }

    @SneakyThrows
    @Test
    void deleteFolder_withUnauthorizedUser_shouldReturnStatus401() {
        MockMultipartFile mainFolderFile = createFile(System.currentTimeMillis() + "-main-folder-file.txt", "main_folder/");
        MockMultipartFile nestedFolderFile = createFile(System.currentTimeMillis() + "-nested-folder-file.txt", "nested_folder/");
        uploadFile(mainFolderFile, "");
        uploadFile(nestedFolderFile, "main_folder/");

        mvc.perform(MockMvcRequestBuilders.delete("/api/resource")
                        .param("path", "main_folder/"))
                .andExpect(status().isUnauthorized());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void deleteFile_whenFileDoesNotExist_shouldReturnStatus404() {
        mvc.perform(MockMvcRequestBuilders.delete("/api/resource")
                        .param("path", "test-file.txt"))
                .andExpect(status().isNotFound());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void deleteFolder_whenFolderDoesNotExist_shouldReturnStatus404() {
        mvc.perform(MockMvcRequestBuilders.delete("/api/resource")
                        .param("path", "main_folder/"))
                .andExpect(status().isNotFound());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void downloadFile_shouldReturnStatus200() {
        String fileName = System.currentTimeMillis() + "-test-file.txt";
        MockMultipartFile file = createFile(fileName, "");
        uploadFile(file, "");

        mvc.perform(get("/api/resource/download")
                        .param("path", fileName))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void downloadFolder_shouldReturnStatus200() {
        MockMultipartFile mainFolderFile = createFile(System.currentTimeMillis() + "-main-folder-file.txt", "main_folder/");
        MockMultipartFile nestedFolderFile = createFile(System.currentTimeMillis() + "-nested-folder-file.txt", "nested_folder/");
        uploadFile(mainFolderFile, "");
        uploadFile(nestedFolderFile, "main_folder/");

        mvc.perform(get("/api/resource/download")
                        .param("path", "main_folder/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/zip"));
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void downloadEmptyFolder_ShouldReturnStatus200() {
        mvc.perform(post("/api/directory")
                .param("path", "new_empty_folder/"));

        mvc.perform(get("/api/resource/download")
                        .param("path", "new_empty_folder/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/zip"));
    }


    @SneakyThrows
    @Test
    @WithMockCustomUser
    void downloadFile_withEmptyPath_shouldReturnStatus400() {
        String fileName = System.currentTimeMillis() + "-test-file.txt";
        MockMultipartFile file = createFile(fileName, "");
        uploadFile(file, "");

        mvc.perform(get("/api/resource/download"))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void downloadFile_withInvalidPath_shouldReturnStatus400() {
        String fileName = System.currentTimeMillis() + "-test-file.txt";
        MockMultipartFile file = createFile(fileName, "");
        uploadFile(file, "");

        mvc.perform(get("/api/resource/download")
                        .param("path", fileName + "%%^&^))"))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void downloadFolder_withEmptyPath_shouldReturnStatus400() {
        MockMultipartFile mainFolderFile = createFile(System.currentTimeMillis() + "-main-folder-file.txt", "main_folder/");
        MockMultipartFile nestedFolderFile = createFile(System.currentTimeMillis() + "-nested-folder-file.txt", "nested_folder/");
        uploadFile(mainFolderFile, "");
        uploadFile(nestedFolderFile, "main_folder/");

        mvc.perform(get("/api/resource/download"))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void downloadFolder_withInvalidPath_shouldReturnStatus400() {
        MockMultipartFile mainFolderFile = createFile(System.currentTimeMillis() + "-main-folder-file.txt", "main_folder/");
        MockMultipartFile nestedFolderFile = createFile(System.currentTimeMillis() + "-nested-folder-file.txt", "nested_folder/");
        uploadFile(mainFolderFile, "");
        uploadFile(nestedFolderFile, "main_folder/");

        mvc.perform(get("/api/resource/download")
                        .param("path", "%%^&^))main_folder/"))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    void downloadFile_withUnauthorizedUser_shouldReturnStatus401() {
        String fileName = System.currentTimeMillis() + "-test-file.txt";
        MockMultipartFile file = createFile(fileName, "");
        uploadFile(file, "");

        mvc.perform(get("/api/resource/download")
                        .param("path", fileName))
                .andExpect(status().isUnauthorized());
    }

    @SneakyThrows
    @Test
    void downloadFolder_withUnauthorizedUser_shouldReturnStatus401() {
        MockMultipartFile mainFolderFile = createFile(System.currentTimeMillis() + "-main-folder-file.txt", "main_folder/");
        MockMultipartFile nestedFolderFile = createFile(System.currentTimeMillis() + "-nested-folder-file.txt", "nested_folder/");
        uploadFile(mainFolderFile, "");
        uploadFile(nestedFolderFile, "main_folder/");

        mvc.perform(get("/api/resource/download")
                        .param("path", "%%^&^))main_folder/"))
                .andExpect(status().isUnauthorized());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void downloadNonExistentFile_shouldReturnStatus404() {
        String fileName = System.currentTimeMillis() + "-test-file.txt";

        mvc.perform(get("/api/resource/download")
                        .param("path", fileName))
                .andExpect(status().isNotFound());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void downloadNonExistentFolder_shouldReturnStatus404() {
        mvc.perform(get("/api/resource/download")
                        .param("path", "main_folder/"))
                .andExpect(status().isNotFound());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void moveFile_shouldReturnStatus200() {
        String fileName = System.currentTimeMillis() + "-test-file.txt";
        MockMultipartFile file = createFile(fileName, "");
        uploadFile(file, "");

        mvc.perform(get("/api/resource/move")
                        .param("from", fileName)
                        .param("to", "new folder/" + fileName))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.path", is("new folder/")))
                .andExpect(jsonPath("$.name", is(fileName)))
                .andExpect(jsonPath("$.size", is(file.getBytes().length)))
                .andExpect(jsonPath("$.type", is("FILE")));
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void renameFile_shouldReturnStatus200() {
        String fileName = System.currentTimeMillis() + "-test-file.txt";
        MockMultipartFile file = createFile(fileName, "");
        uploadFile(file, "");

        mvc.perform(get("/api/resource/move")
                        .param("from", fileName)
                        .param("to", "-new-" + fileName))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.path", is("")))
                .andExpect(jsonPath("$.name", is("-new-" + fileName)))
                .andExpect(jsonPath("$.size", is(file.getBytes().length)))
                .andExpect(jsonPath("$.type", is("FILE")));
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void moveFolder_shouldReturnStatus200() {
        MockMultipartFile mainFolderFile = createFile(System.currentTimeMillis() + "-main-folder-file.txt", "main_folder/");
        MockMultipartFile nestedFolderFile = createFile(System.currentTimeMillis() + "-nested-folder-file.txt", "nested_folder/");
        uploadFile(mainFolderFile, "");
        uploadFile(nestedFolderFile, "main_folder/");

        mvc.perform(get("/api/resource/move")
                        .param("from", "main_folder/nested_folder/")
                        .param("to", "nested_folder/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.path", is("nested_folder/")))
                .andExpect(jsonPath("$.name", is("nested_folder/")))
                .andExpect(jsonPath("$.type", is("DIRECTORY")));

    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void renameFolder_shouldReturnStatus200() {
        MockMultipartFile mainFolderFile = createFile(System.currentTimeMillis() + "-main-folder-file.txt", "main_folder/");
        MockMultipartFile nestedFolderFile = createFile(System.currentTimeMillis() + "-nested-folder-file.txt", "nested_folder/");
        uploadFile(mainFolderFile, "");
        uploadFile(nestedFolderFile, "main_folder/");

        mvc.perform(get("/api/resource/move")
                        .param("from", "main_folder/")
                        .param("to", "new main folder/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.path", is("new main folder/")))
                .andExpect(jsonPath("$.name", is("new main folder/")))
                .andExpect(jsonPath("$.type", is("DIRECTORY")));
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void renameFile_withEmptyFromPath_shouldReturnStatus400() {
        String fileName = System.currentTimeMillis() + "-test-file.txt";
        MockMultipartFile file = createFile(fileName, "");
        uploadFile(file, "");

        mvc.perform(get("/api/resource/move")
                        .param("to", "new" + fileName))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void renameFile_withEmptyToPath_shouldReturnStatus400() {
        String fileName = System.currentTimeMillis() + "-test-file.txt";
        MockMultipartFile file = createFile(fileName, "");
        uploadFile(file, "");

        mvc.perform(get("/api/resource/move")
                        .param("from", fileName))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void renameFile_withInvalidFromPath_shouldReturnStatus400() {
        String fileName = System.currentTimeMillis() + "-test-file.txt";
        MockMultipartFile file = createFile(fileName, "");
        uploadFile(file, "");

        mvc.perform(get("/api/resource/move")
                        .param("from", "^%$#@" + fileName)
                        .param("to", "new" + fileName))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void renameFile_withInvalidToPath_shouldReturnStatus400() {
        String fileName = System.currentTimeMillis() + "-test-file.txt";
        MockMultipartFile file = createFile(fileName, "");
        uploadFile(file, "");

        mvc.perform(get("/api/resource/move")
                        .param("from", fileName)
                        .param("to", "^%$#@" + fileName))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void renameFolder_withEmptyFromPath_shouldReturnStatus400() {
        MockMultipartFile mainFolderFile = createFile(System.currentTimeMillis() + "-main-folder-file.txt", "main_folder/");
        MockMultipartFile nestedFolderFile = createFile(System.currentTimeMillis() + "-nested-folder-file.txt", "nested_folder/");
        uploadFile(mainFolderFile, "");
        uploadFile(nestedFolderFile, "main_folder/");

        mvc.perform(get("/api/resource/move")
                        .param("to", "new main folder/"))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void renameFolder_withEmptyToPath_shouldReturnStatus400() {
        MockMultipartFile mainFolderFile = createFile(System.currentTimeMillis() + "-main-folder-file.txt", "main_folder/");
        MockMultipartFile nestedFolderFile = createFile(System.currentTimeMillis() + "-nested-folder-file.txt", "nested_folder/");
        uploadFile(mainFolderFile, "");
        uploadFile(nestedFolderFile, "main_folder/");

        mvc.perform(get("/api/resource/move")
                        .param("from", "main_folder/"))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void renameFolder_withInvalidFromPath_shouldReturnStatus400() {
        MockMultipartFile mainFolderFile = createFile(System.currentTimeMillis() + "-main-folder-file.txt", "main_folder/");
        MockMultipartFile nestedFolderFile = createFile(System.currentTimeMillis() + "-nested-folder-file.txt", "nested_folder/");
        uploadFile(mainFolderFile, "");
        uploadFile(nestedFolderFile, "main_folder/");

        mvc.perform(get("/api/resource/move")
                        .param("from", "%^$#&main_folder/")
                        .param("to", "new main folder/"))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void renameFolder_withInvalidToPath_shouldReturnStatus400() {
        MockMultipartFile mainFolderFile = createFile(System.currentTimeMillis() + "-main-folder-file.txt", "main_folder/");
        MockMultipartFile nestedFolderFile = createFile(System.currentTimeMillis() + "-nested-folder-file.txt", "nested_folder/");
        uploadFile(mainFolderFile, "");
        uploadFile(nestedFolderFile, "main_folder/");

        mvc.perform(get("/api/resource/move")
                        .param("from", "main_folder/")
                        .param("to", "%^$#new main folder/"))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    void renameFile_withUnauthorizedUser_shouldReturnStatus401() {
        String fileName = System.currentTimeMillis() + "-test-file.txt";
        MockMultipartFile file = createFile(fileName, "");
        uploadFile(file, "");

        mvc.perform(get("/api/resource/move")
                        .param("from", fileName)
                        .param("to", fileName))
                .andExpect(status().isUnauthorized());
    }

    @SneakyThrows
    @Test
    void renameFolder_withUnauthorizedUser_shouldReturnStatus401() {
        MockMultipartFile mainFolderFile = createFile(System.currentTimeMillis() + "-main-folder-file.txt", "main_folder/");
        MockMultipartFile nestedFolderFile = createFile(System.currentTimeMillis() + "-nested-folder-file.txt", "nested_folder/");
        uploadFile(mainFolderFile, "");
        uploadFile(nestedFolderFile, "main_folder/");

        mvc.perform(get("/api/resource/move")
                        .param("from", "main_folder/")
                        .param("to", "new main folder/"))
                .andExpect(status().isUnauthorized());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void renameNonExistentFile_shouldReturnStatus404() {
        String fileName = System.currentTimeMillis() + "-test-file.txt";

        mvc.perform(get("/api/resource/move")
                        .param("from", fileName)
                        .param("to", "new" + fileName))
                .andExpect(status().isNotFound());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void renameNonExistentFolder_shouldReturnStatus404() {
        mvc.perform(get("/api/resource/move")
                        .param("from", "main_folder/")
                        .param("to", "new main folder/"))
                .andExpect(status().isNotFound());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void renameFile_withAlreadyExistingName_shouldReturnStatus409() {
        String testFileName = System.currentTimeMillis() + "-test-file.txt";
        String existingFileName = System.currentTimeMillis() + "-existing-file.txt";
        MockMultipartFile mainFolderFile = createFile(testFileName, "");
        MockMultipartFile nestedFolderFile = createFile(existingFileName, "");
        uploadFile(mainFolderFile, "");
        uploadFile(nestedFolderFile, "");

        mvc.perform(get("/api/resource/move")
                        .param("from", testFileName)
                        .param("to", existingFileName))
                .andExpect(status().isConflict());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void moveFile_withAlreadyExistingName_shouldReturnStatus409() {
        String testFileName = System.currentTimeMillis() + "-test-file.txt";
        MockMultipartFile mainFolderFile = createFile(testFileName, "");
        MockMultipartFile nestedFolderFile = createFile(testFileName, "main_folder/");
        uploadFile(mainFolderFile, "");
        uploadFile(nestedFolderFile, "");

        mvc.perform(get("/api/resource/move")
                        .param("from", testFileName)
                        .param("to", "main_folder/" + testFileName))
                .andExpect(status().isConflict());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void renameFolder_withAlreadyExistingName_shouldReturnStatus409() {
        MockMultipartFile mainFolderFile = createFile(System.currentTimeMillis() + "-main-folder-file.txt", "main_folder/");
        uploadFile(mainFolderFile, "");

        mvc.perform(post("/api/directory")
                .param("path", "new main folder/"));

        mvc.perform(get("/api/resource/move")
                        .param("from", "main_folder/")
                        .param("to", "new main folder/"))
                .andExpect(status().isConflict());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void moveFolder_withAlreadyExistingName_shouldReturnStatus409() {
        MockMultipartFile mainFolderFile = createFile(System.currentTimeMillis() + "-main-folder-file.txt", "main_folder/nested_folder/");
        uploadFile(mainFolderFile, "");

        mvc.perform(post("/api/directory")
                .param("path", "nested_folder/"));

        mvc.perform(get("/api/resource/move")
                        .param("from", "main_folder/nested_folder/")
                        .param("to", "nested_folder/"))
                .andExpect(status().isConflict());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void searchFile_shouldReturnStatus200() {
        String firstFileName = System.currentTimeMillis() + "-first-file.txt";
        String secondFileName = System.currentTimeMillis() + "-second-file.txt";
        MockMultipartFile firstFile = createFile(firstFileName, "");
        MockMultipartFile secondFile = createFile(secondFileName, "nested_folder/");
        uploadFile(firstFile, "");
        uploadFile(secondFile, "");

        mvc.perform(get("/api/resource/search")
                        .param("query", "first"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", instanceOf(List.class)))
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].name", is(firstFileName)))
                .andExpect(jsonPath("$[0].path", is("")))
                .andExpect(jsonPath("$[0].size", is(("Text from " + firstFileName).getBytes().length)))
                .andExpect(jsonPath("$[0].type", is("FILE")));
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void searchFolder_shouldReturnStatus200() {
        mvc.perform(post("/api/directory")
                .param("path", "folder/"));

        mvc.perform(post("/api/directory")
                .param("path", "folder/nested folder/"));

        mvc.perform(get("/api/resource/search")
                        .param("query", "nes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", instanceOf(List.class)))
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].name", is("nested folder/")))
                .andExpect(jsonPath("$[0].path", is("folder/nested folder/")))
                .andExpect(jsonPath("$[0].type", is("DIRECTORY")));
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void searchFile_withEmptyQuery_shouldReturnStatus400() {
        String firstFileName = System.currentTimeMillis() + "-first-file.txt";
        String secondFileName = System.currentTimeMillis() + "-second-file.txt";
        MockMultipartFile firstFile = createFile(firstFileName, "");
        MockMultipartFile secondFile = createFile(secondFileName, "nested_folder/");
        uploadFile(firstFile, "");
        uploadFile(secondFile, "");

        mvc.perform(get("/api/resource/search"))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void searchFolder_withEmptyQuery_shouldReturnStatus400() {
        mvc.perform(post("/api/directory")
                .param("path", "folder/"));

        mvc.perform(post("/api/directory")
                .param("path", "folder/nested folder/"));

        mvc.perform(get("/api/resource/search"))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    void searchFile_withUnauthorizedUser_shouldReturnStatus401() {
        String firstFileName = System.currentTimeMillis() + "-first-file.txt";
        String secondFileName = System.currentTimeMillis() + "-second-file.txt";
        MockMultipartFile firstFile = createFile(firstFileName, "");
        MockMultipartFile secondFile = createFile(secondFileName, "nested_folder/");
        uploadFile(firstFile, "");
        uploadFile(secondFile, "");

        mvc.perform(get("/api/resource/search")
                        .param("query", "first"))
                .andExpect(status().isUnauthorized());
    }

    @SneakyThrows
    @Test
    void searchFolder_withUnauthorizedUser_shouldReturnStatus401() {
        mvc.perform(post("/api/directory")
                .param("path", "folder/"));

        mvc.perform(post("/api/directory")
                .param("path", "folder/nested folder/"));

        mvc.perform(get("/api/resource/search")
                        .param("query", "nes"))
                .andExpect(status().isUnauthorized());
    }
}
