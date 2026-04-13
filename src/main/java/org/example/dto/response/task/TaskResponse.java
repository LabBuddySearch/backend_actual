package org.example.dto.response.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.example.dto.common.TestCaseDto;

import java.util.List;

@Data
@Builder
@Schema(description = "Task data")
public class TaskResponse {
    @Schema(
            description = "Task ID",
            example = "1231231"
    )
    private Long id;

    @Schema(
            description = "Author name",
            example = "Иван Иванов"
    )
    private String author;

    @Schema(
            description = "Task title",
            example = "Быстрая сортировка"
    )
    private String title;

    @Schema(
            description = "Task description",
            example = "Реализуйте алгоритм быстрой сортировки"
    )
    private String description;

    @Schema(
            description = "Task time limit in milliseconds",
            example = "1112"
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
