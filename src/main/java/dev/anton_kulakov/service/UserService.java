package dev.anton_kulakov.service;

import dev.anton_kulakov.dto.UserRequestDto;
import dev.anton_kulakov.exception.UsernameAlreadyTakenException;
import dev.anton_kulakov.mapper.UserMapper;
import dev.anton_kulakov.model.User;
import dev.anton_kulakov.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(UserRequestDto userRequestDto) {
        String username = userRequestDto.getUsername();
        String rawPassword = userRequestDto.getPassword();

        String encodedPassword = passwordEncoder.encode(rawPassword);
        User user = userMapper.toUser(userRequestDto);
        user.setPassword(encodedPassword);

        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            log.warn("User creation failed for username '{}'. It might already exist.", username, e);
            throw new UsernameAlreadyTakenException("User with username %s is already exists".formatted(username));
        }
    }
}
