package org.example.dto.response.task;

import lombok.Builder;
import lombok.Data;
import org.example.dto.common.TestCaseDto;

import java.util.List;

@Data
@Builder
public class TaskResponse {
    private Long id;

    private String author;

    private String title;

    private String description;

    private Integer timeLimitMs;

    private Integer memoryLimitMb;

    private List<TestCaseDto> testCaseDtos;


}
