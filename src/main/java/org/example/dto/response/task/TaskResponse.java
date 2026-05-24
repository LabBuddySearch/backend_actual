package org.example.dto.response.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.example.entity.TaskCategory;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Task data")
public class TaskResponse {
    @Schema(
            description = "Task ID",
            example = "1231231"
    )
    private Integer id;

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

    private LocalDateTime deadlineAt;

    private Integer maxAttempts;

    private TaskCategory category;

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
    private List<TestCaseResponse> testCases;

    private Integer assignedGroupId;

    private Integer assignedStudentId;

    private Boolean assignToAllTeacherGroups;

    private StudentTaskProgressResponse studentProgress;

}
