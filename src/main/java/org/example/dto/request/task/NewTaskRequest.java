package org.example.dto.request.task;

import lombok.Data;
import org.example.dto.common.TestCaseDto;

import java.util.List;

@Data
public class NewTaskRequest {
    private String title;

    private String description;

    private Integer timeLimitMs;

    private Integer memoryLimitMb;

    private List<TestCaseDto> testCaseDtos;

}
