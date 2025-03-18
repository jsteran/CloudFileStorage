package dev.anton_kulakov.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResourceInfoDto {
    private String path;
    private String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long size;

    private ResourceTypeEnum type;
}
