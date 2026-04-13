package org.example.dto.response;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@Schema(description = "Error description")
public class ErrorResponse {
    @Schema(
            description = "Error code",
            example = "404"
    )
    private String errorCode;

    @Schema(
            description = "Error description message",
            example = "Object not found"
    )
    private String message;

    @Schema(
            description = "Error throw time",
            example = "2026-12-25T14:30:00"
    )
    private LocalDateTime timestamp;
}
