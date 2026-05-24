package org.example.dto.response.stats;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentTaskStatsDetailResponse {
    private Integer taskId;
    private String taskTitle;
    /** SOLVED | FAILED | IN_PROGRESS | NOT_STARTED */
    private String status;
    private int attemptsUsed;
    private int maxAttempts;
}
