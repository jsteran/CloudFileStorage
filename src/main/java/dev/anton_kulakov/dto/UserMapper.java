package dev.anton_kulakov.dto;

import dev.anton_kulakov.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserRequestDto userRequestDto);
    UserResponseDto toResponseDto(User user);
}
