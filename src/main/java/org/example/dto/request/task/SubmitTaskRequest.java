package org.example.dto.request.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Data for submit task")
public class SubmitTaskRequest {
    @NotNull
    @Schema(
            description = "Task ID",
            example = "123234132",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long id;

    @NotEmpty
    @Schema(
            description = "Code with the solution",
            example = "print('Hello')",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String code;
}
