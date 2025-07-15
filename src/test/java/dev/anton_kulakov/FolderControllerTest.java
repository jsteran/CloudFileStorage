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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class FolderControllerTest extends AbstractControllerIntegrationTest {
    @SneakyThrows
    @Test
    @WithMockCustomUser
    void getFolderContent_shouldReturnStatus200() {
        String mainFolderFileName = System.currentTimeMillis() + "-main-folder-file.txt";
        String nestedFolderFileName = System.currentTimeMillis() + "-nested-folder-file.txt";
        MockMultipartFile mainFolderFile = createFile(mainFolderFileName, "main_folder/");
        MockMultipartFile nestedFolderFile = createFile(nestedFolderFileName, "main_folder/nested_folder/");
        uploadFile(mainFolderFile, "");
        uploadFile(nestedFolderFile, "");

        mvc.perform(MockMvcRequestBuilders.get("/api/directory")
                        .param("path", "main_folder/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", instanceOf(List.class)))
                .andExpect(jsonPath("$.length()", is(2)))

                .andExpect(jsonPath("$[1].path", is("main_folder/nested_folder/")))
                .andExpect(jsonPath("$[1].name", is("nested_folder/")))
                .andExpect(jsonPath("$[1].type", is("DIRECTORY")))

                .andExpect(jsonPath("$[0].path", is("main_folder/")))
                .andExpect(jsonPath("$[0].name", is(mainFolderFileName)))
                .andExpect(jsonPath("$[0].size", is(mainFolderFile.getBytes().length)))
                .andExpect(jsonPath("$[0].type", is("FILE")));
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void getFolderContent_withEmptyPath_shouldReturnStatus400() {
        String mainFolderFileName = System.currentTimeMillis() + "-main-folder-file.txt";
        String nestedFolderFileName = System.currentTimeMillis() + "-nested-folder-file.txt";
        MockMultipartFile mainFolderFile = createFile(mainFolderFileName, "main_folder/");
        MockMultipartFile nestedFolderFile = createFile(nestedFolderFileName, "main_folder/nested_folder/");
        uploadFile(mainFolderFile, "");
        uploadFile(nestedFolderFile, "");

        mvc.perform(MockMvcRequestBuilders.get("/api/directory"))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void getFolderContent_withInvalidPath_shouldReturnStatus400() {
        String mainFolderFileName = System.currentTimeMillis() + "-main-folder-file.txt";
        String nestedFolderFileName = System.currentTimeMillis() + "-nested-folder-file.txt";
        MockMultipartFile mainFolderFile = createFile(mainFolderFileName, "main_folder/");
        MockMultipartFile nestedFolderFile = createFile(nestedFolderFileName, "main_folder/nested_folder/");
        uploadFile(mainFolderFile, "");
        uploadFile(nestedFolderFile, "");

        mvc.perform(MockMvcRequestBuilders.get("/api/directory")
                        .param("path", "main_folder&^%%$#/"))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    void getFolderContent_withUnauthorizedUser_shouldReturnStatus401() {
        mvc.perform(MockMvcRequestBuilders.get("/api/directory")
                        .param("path", "main_folder/"))
                .andExpect(status().isUnauthorized());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void getNonExistentFolderContent_shouldReturnStatus404() {
        mvc.perform(MockMvcRequestBuilders.get("/api/directory")
                        .param("path", "main_folder/"))
                .andExpect(status().isNotFound());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void createEmptyFolder_shouldReturnStatus201() {
        mvc.perform(MockMvcRequestBuilders.post("/api/directory")
                        .param("path", "folder/"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.path", is("folder/")))
                .andExpect(jsonPath("$.name", is("folder/")))
                .andExpect(jsonPath("$.type", is("DIRECTORY")));
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void createEmptyFolder_withEmptyPath_shouldReturnStatus400() {
        mvc.perform(MockMvcRequestBuilders.post("/api/directory"))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void createEmptyFolder_withInvalidPath_shouldReturnStatus400() {
        mvc.perform(MockMvcRequestBuilders.post("/api/directory")
                        .param("path", "^&&%$#folder/"))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    void createEmptyFolder_withUnauthorizedUser_shouldReturnStatus401() {
        mvc.perform(MockMvcRequestBuilders.post("/api/directory")
                        .param("path", "folder/"))
                .andExpect(status().isUnauthorized());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void createEmptyFolder_withNonExistentParentFolder_shouldReturnStatus404() {
        mvc.perform(MockMvcRequestBuilders.post("/api/directory")
                        .param("path", "folder/nested folder/"))
                .andExpect(status().isNotFound());
    }

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void createAlreadyExistingFolder_shouldReturnStatus409() {
        mvc.perform(MockMvcRequestBuilders.post("/api/directory")
                .param("path", "folder/"));

        mvc.perform(MockMvcRequestBuilders.post("/api/directory")
                        .param("path", "folder/"))
                .andExpect(status().isConflict());
    }
}
