package org.example.dto.request.task;

import lombok.Data;
import org.example.dto.common.TestCase;

@Data
public class NewTaskRequest {
    private String title;

    private String description;

    private Integer timeLimitMs;

    private Integer memoryLimitMb;

    private TestCase[] testCases;

}
