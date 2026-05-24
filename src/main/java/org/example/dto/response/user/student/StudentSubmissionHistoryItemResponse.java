package org.example.dto.response.user.student;

import lombok.Builder;
import lombok.Data;
import org.example.entity.Status;

import java.time.LocalDateTime;

@Data
@Builder
public class StudentSubmissionHistoryItemResponse {
    private Integer submissionId;
    private Integer taskId;
    private String taskTitle;
    private Status status;
    private String language;
    private LocalDateTime createdAt;
}
