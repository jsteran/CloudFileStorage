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

        mvc.perform(get("/api/resource?path=test-file.txt"))
                .andExpect(status().isOk());
    }

    // аплоад: отсутствие тела запроса кидает ошибку и код 400

    @SneakyThrows
    @Test
    @WithMockCustomUser
    void upload_withEmptyRequestBody_shouldReturnStatus400() {
        mvc.perform(multipart("/api/resource")
                        .param("path", ""))
                .andExpect(status().isBadRequest());
    }


    // аплоад: невалидный путь папки кидает ошибку и код 400
    // аплоад: отсутствующий путь папки кидает ошибку и код 400

    // аплоад: попытка повторной загрузки файла кидает ошибку и код 409
    // аплоад: попытка повторной загрузки папки кидает ошибку и код 409

    // гетИнфо: запрос файла выдает код 200 ок
    // гетИнфо: запрос папки выдает код 200 ок

    // гетИнфо: невалидный путь файла кидает ошибку и код 400
    // гетИнфо: отсутствующий путь файла кидает ошибку и код 400

    // гетИнфо: невалидный путь папки кидает ошибку и код 400
    // гетИнфо: отсутствующий путь папки кидает ошибку и код 400

    // гетИнфо: отсутствие запрашиваемого файла кидает ошибку и код 404
    // гетИнфо: отсутствие запрашиваемого папки кидает ошибку и код 404

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
