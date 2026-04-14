package org.example.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

// Кастомный компонент, который возвращает понятный json, если пользователь не имеет достаточно прав
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException ex) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        String timestamp = LocalDateTime.now().toString();
        response.getWriter().write(String.format("""
                    {
                      "errorCode": "UNAUTHORIZED",
                      "message": "Invalid or missing authentication token",
                      "timestamp": %s
                    }
                """, timestamp));
    }
}

