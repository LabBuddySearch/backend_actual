package org.example.dto.response.stats;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentBriefStatsResponse {
    private Integer studentId;
    private String fullName;
    private String email;
}
