package org.example.dto.request.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Schema(description = "Data for edit task")
@AllArgsConstructor
@NoArgsConstructor
public class EditTaskRequest {
    @Schema(
            description = "New task description",
            example = "Реализуйте алгоритм быстрой сортировки"
    )
    private String description;

    @Schema(
            description = "Task time limit in milliseconds",
            example = "1000"
    )
    private Integer timeLimitMs;

    @Schema(
            description = "Task memory limit in Mb",
            example = "128"
    )
    private Integer memoryLimitMb;

    @Schema(
            description = "List of task tests",
            example = "[{\"inputData\": \"ABBCABA\", \"expectedOutput\": \"text\", \"isHidden\": true}]"
    )
    private List<TestCaseRequest> testCases;
}
