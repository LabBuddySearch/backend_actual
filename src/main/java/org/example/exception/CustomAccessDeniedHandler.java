package org.example.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

// Кастомный компонент, который возвращает понятный json, если пользователь не прошел аутентификацию
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        String timestamp = LocalDateTime.now().toString();
        response.getWriter().write(String.format("""
                    {
                      "errorCode": "ACCESS_DENIED",
                      "message": "You don't have permission",
                      "timestamp": %s
                    }
                """, timestamp));
    }
}
