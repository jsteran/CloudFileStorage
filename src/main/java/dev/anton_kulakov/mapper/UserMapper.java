package dev.anton_kulakov.mapper;

import dev.anton_kulakov.dto.UserRequestDto;
import dev.anton_kulakov.dto.UserResponseDto;
import dev.anton_kulakov.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserRequestDto userRequestDto);
    UserResponseDto toResponseDto(User user);
}
