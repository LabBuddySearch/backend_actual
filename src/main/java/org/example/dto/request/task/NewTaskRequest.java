package org.example.dto.request.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.entity.TaskCategory;

import java.time.LocalDateTime;
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
            example = "1000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer timeLimitMs;

    @NotNull
    @Schema(
            description = "Task memory limit in Mb",
            example = "128",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer memoryLimitMb;

    @NotNull
    @Schema(description = "Submission deadline", example = "2026-06-01T23:59:00")
    private LocalDateTime deadlineAt;

    @NotNull
    @Schema(description = "Maximum submission attempts per student", example = "3")
    private Integer maxAttempts;

    @NotNull
    @Schema(description = "Task category: ALGORITHMS, TEXT, LOGICAL", example = "ALGORITHMS")
    private TaskCategory category;

    @NotEmpty
    @Schema(
            description = "List of task tests",
            example = "[{\"inputData\": \"ABBCABA\", \"expectedOutput\": \"text\", \"isHidden\": true}]",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<TestCaseRequest> testCases;

    @Schema(description = "Assign task to a group (optional)")
    private Integer assignedGroupId;

    @Schema(description = "Assign task to a student (optional)")
    private Integer assignedStudentId;

    @Schema(description = "Assign to all students in teacher active groups")
    private Boolean assignToAllTeacherGroups;

}
