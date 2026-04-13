package org.example.dto.response.user.student;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Submission data on profile page")
public class StudentProfileSubmission {
    @Schema(
            description = "Task title",
            example = "Быстрая сортировка"
    )
    private String title;

    @Schema(
            description = "Submission status",
            example = "WRONG"
    )
    private String status;

    @Schema(
            description = "Submission language",
            example = "Java"
    )
    private String language;

    @Schema(
            description = "Submission date",
            example = "2026-12-25T14:30:00"
    )
    private LocalDateTime dateTime;
}
