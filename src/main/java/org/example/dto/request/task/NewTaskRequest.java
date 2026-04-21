package org.example.dto.request.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Data for create new task")
public class NewTaskRequest {
    @Schema(
            description = "Task title",
            example = "Быстрая сортировка",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    private String title;

    @NotBlank
    @Schema(
            description = "Task description",
            example = "Реализуйте алгоритм быстрой сортировки",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String description;

    @NotNull
    @Schema(
            description = "Task time limit in milliseconds",
            example = "111112211",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer timeLimitMs;

    @NotNull
    @Schema(
            description = "Task memory limit in Mb",
            example = "10",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer memoryLimitMb;

    @NotEmpty
    @Schema(
            description = "List of task tests",
            example = "[{\"inputData\": \"ABBCABA\", \"expectedOutput\": 5, \"isHidden\": true}]",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<TestCaseRequest> testCases;

}
