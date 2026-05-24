package org.example.dto.response.stats;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TaskDashboardStatsResponse {
    private Integer taskId;
    private String taskTitle;
    private List<StudentBriefStatsResponse> solvedStudents;
    private List<StudentFailedStatsResponse> failedStudents;
}
