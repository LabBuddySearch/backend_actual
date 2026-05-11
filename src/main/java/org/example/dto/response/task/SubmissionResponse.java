package org.example.dto.response.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.entity.Status;

@Data
@Builder
@Schema(description = "Submission response data")
@AllArgsConstructor
@NoArgsConstructor
public class SubmissionResponse {
    @Schema(
            description = "Submission ID",
            example = "1111"
    )
    private Integer id;

    @Schema(
            description = "Вердикт проверки (см. README / документацию по статусам)",
            example = "WRONG_ANSWER"
    )
    private Status status;

    @Schema(
            description = "Submission execution time in milliseconds",
            example = "11121"
    )
    private int executionTimeMs;

    @Schema(
            description = "Submission programming language",
            example = "Java"
    )
    private String language;

    @Schema(
            description = "Submission standard output",
            example = "Hello"
    )
    private String stdout;

    @Schema(
            description = "Submission standard error",
            example = "0"
    )
    private String stderr;
}
