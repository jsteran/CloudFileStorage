package dev.anton_kulakov.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.anton_kulakov.dto.UserRequestDto;
import dev.anton_kulakov.util.SecurityContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JsonAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final ObjectMapper objectMapper;
    private final SecurityContextUtil securityContextUtil;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse resp) {
        try {
            UserRequestDto userRequestDto = objectMapper.readValue(req.getInputStream(), UserRequestDto.class);
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userRequestDto.getUsername(), userRequestDto.getPassword());
            Authentication authentication = this.getAuthenticationManager().authenticate(usernamePasswordAuthenticationToken);
            securityContextUtil.setAuthentication(authentication, req, resp);
            return authentication;
        } catch (IOException e) {
            log.error("Failed to parse authentication request JSON. Invalid format or I/O error", e);
            throw new AuthenticationServiceException("Failed to parse login request", e);
        }
    }
}
