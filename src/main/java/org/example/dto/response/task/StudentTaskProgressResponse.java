package org.example.dto.response.task;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StudentTaskProgressResponse {
    private int attemptsUsed;
    private int attemptsRemaining;
    private int maxAttempts;
    private boolean deadlineExpired;
    private boolean canSubmit;
    private boolean solved;
    private List<SubmissionSummaryResponse> recentSubmissions;
}
