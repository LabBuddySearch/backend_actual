package org.example.dto.request.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.dto.common.TestCaseDto;

import java.util.List;

@Data
@Schema(description = "Data for edit task")
public class EditTaskRequest {
    @NotNull
    @Schema(
            description = "Task ID",
            example = "123234132",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long id;

    @Schema(
            description = "New task description",
            example = "Реализуйте алгоритм быстрой сортировки"
    )
    private String description;

    @Schema(
            description = "Task time limit in milliseconds",
            example = "111112211"
    )
    private Integer timeLimitMs;

    @Schema(
            description = "Task memory limit in Mb",
            example = "10"
    )
    private Integer memoryLimitMb;

    @Schema(
            description = "List of task tests",
            example = """
                    [
                        {
                            "inputData": 'ABBCABA',
                            "expectedOutput": 5,
                            "isHidden": true
                        }
                    ]
                    """
    )
    private List<TestCaseDto> testCaseDtos;
}
