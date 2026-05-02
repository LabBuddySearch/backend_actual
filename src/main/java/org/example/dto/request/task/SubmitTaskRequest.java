package org.example.dto.request.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
@Schema(description = "Data for submit task")
public class SubmitTaskRequest {
    @NotEmpty
    @Schema(
            description = "Programming language",
            example = "PYTHON",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String language;

    @NotEmpty
    @Schema(
            description = "Code with the solution",
            example = "print(\"Hello\")",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String sourceCode;
}
