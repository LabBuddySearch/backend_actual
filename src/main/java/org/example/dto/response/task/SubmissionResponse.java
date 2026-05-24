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
    private Integer id;
    private Status status;
    private int executionTimeMs;
    private String language;
    private String stdout;
    private String stderr;
    private String message;
    private boolean passed;
    private boolean syntaxError;
    private Integer failedTestIndex;
    private int attemptsUsed;
    private int attemptsRemaining;
}
