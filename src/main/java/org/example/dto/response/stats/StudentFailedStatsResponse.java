package org.example.dto.response.stats;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentFailedStatsResponse {
    private Integer studentId;
    private String fullName;
    private String email;
    /** DEADLINE_EXCEEDED | ATTEMPTS_EXHAUSTED */
    private String failureReason;
}
