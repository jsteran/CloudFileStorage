package dev.anton_kulakov.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.anton_kulakov.model.ResourceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO for displaying information about a folder or file")
public class ResourceInfoDto {
    @Schema(description = "The path to the file or folder", example = "folder/file.txt")
    private String path;

    @Schema(description = "The name to the file or folder", example = "file.txt")
    private String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "File size (only applicable to files)", example = "29")
    private Long size;

    @Schema(description = "Type of resource")
    private ResourceTypeEnum type;
}
