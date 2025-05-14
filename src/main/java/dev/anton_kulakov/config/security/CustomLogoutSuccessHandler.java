package dev.anton_kulakov.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.util.Map;

@RequiredArgsConstructor
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {
    private final ObjectMapper objectMapper;
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        try {
            int statusCode = (authentication == null) ? HttpServletResponse.SC_UNAUTHORIZED : HttpServletResponse.SC_NO_CONTENT;
            response.setStatus(statusCode);

            if (authentication == null) {
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                Map<String, String> responseBody = Map.of("message", "Invalid credentials");
                objectMapper.writeValue(response.getWriter(), responseBody);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
