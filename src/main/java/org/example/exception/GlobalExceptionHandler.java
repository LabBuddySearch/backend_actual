package org.example.exception;

import org.example.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimit(RateLimitExceededException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .errorCode("RATE_LIMIT_EXCEEDED")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(CodeGuardException.class)
    public ResponseEntity<ErrorResponse> handleCodeGuardException(CodeGuardException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 2. Ошибка валидации (например, пустой email при регистрации)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponse error = ErrorResponse.builder()
                .errorCode("VALIDATION_ERROR")
                .message("Ошибка в данных: " + details)
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 3. Ошибка доступа (Spring Security)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .errorCode("ACCESS_DENIED")
                .message("У вас недостаточно прав для этого действия")
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    // 4. Все остальные непредвиденные ошибки
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        ErrorResponse error = ErrorResponse.builder()
                .errorCode("INTERNAL_SERVER_ERROR")
                .message(ex.getClass().getName() + " " + ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 5. Объект не найден
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(Exception ex) {
        ErrorResponse error = ErrorResponse.builder()
                .errorCode("NOT_FOUND")
                .message("Объект не найден")
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // 6. Ошибка десериализации (не получилось прочитать входящий запрос, ошибка до валидации через @Valid)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageReadException(HttpMessageNotReadableException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .errorCode("BAD_REQUEST")
                .message("Ошибка в данных: " + ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 7. Объект не уникален
    @ExceptionHandler(NotUniqueObjectException.class)
    public ResponseEntity<ErrorResponse> handleNotUniqueException(Exception ex) {
        ErrorResponse error = ErrorResponse.builder()
                .errorCode("CONFLICT")
                .message("Такой объект уже существует")
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    // 8. Ошибка логина (неправильный пароль)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleLoginException(Exception ex) {
        ErrorResponse error = ErrorResponse.builder()
                .errorCode("UNAUTHORIZED")
                .message("Неверные данные для входа")
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    // 9. Ошибка аутентификации
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(Exception ex) {
        ErrorResponse error = ErrorResponse.builder()
                .errorCode("DOES_NOT_AUTHENTICATE")
                .message("Ошибка аутентификации")
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    // 10. Ошибка в Docker-контейнере
    @ExceptionHandler(DockerExecutionException.class)
    public ResponseEntity<ErrorResponse> handleDockerExecutionException(Exception ex) {
        ErrorResponse error = ErrorResponse.builder()
                .errorCode("DOCKER_EXECUTION_EXCEPTION")
                .message("Ошибка в Docker-контейнере")
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
