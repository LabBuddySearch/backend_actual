package org.example.dto.response.user.student;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentTaskResponse {
    private String title;

    private String description;

    private Integer timeLimitMs;

    private Integer memoryLimitMb;

    private StudentTestCase[] testCases;


}
