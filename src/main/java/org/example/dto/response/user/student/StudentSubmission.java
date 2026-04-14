package org.example.dto.response.user.student;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Submission data")
public class StudentSubmission {
    @Schema(
            description = "Submission ID",
            example = "12312311"
    )
    private Integer id;

    @Schema(
            description = "Submission status",
            example = "WRONG"
    )
    private String status;

    @Schema(
            description = "Submission programming language",
            example = "Java"
    )
    private String language;

    @Schema(
            description = "Submission execution time in milliseconds",
            example = "11121"
    )
    private int executionTimeMs;
}
