package org.example.dto.request.task;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.dto.common.TestCaseDto;

import java.util.List;

@Data
public class EditTaskRequest {
    @NotNull
    private Long id;

    private String description;

    private Integer timeLimitMs;

    private Integer memoryLimitMb;

    private List<TestCaseDto> testCaseDtos;
}
