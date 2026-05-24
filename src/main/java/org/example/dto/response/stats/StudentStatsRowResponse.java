package org.example.dto.response.stats;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StudentStatsRowResponse {
    private Integer studentId;
    private String fullName;
    private String email;
    private int solvedTasksCount;
    private List<StudentTaskStatsDetailResponse> tasks;
}
