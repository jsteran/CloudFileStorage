package dev.anton_kulakov.service;

import dev.anton_kulakov.dto.UserRequestDto;
import dev.anton_kulakov.mapper.UserMapper;
import dev.anton_kulakov.exception.UsernameAlreadyTakenException;
import dev.anton_kulakov.model.User;
import dev.anton_kulakov.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(UserRequestDto userRequestDto) {
        String username = userRequestDto.getUsername();

        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyTakenException("User with username %s is already exists".formatted(username));
        }

        String encodedPassword = passwordEncoder.encode(userRequestDto.getPassword());
        userRequestDto.setPassword(encodedPassword);
        User user = userMapper.toUser(userRequestDto);
        return userRepository.save(user);
    }
}
