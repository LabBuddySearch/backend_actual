package org.example.dto.response.task;

import lombok.Builder;
import lombok.Data;
import org.example.entity.Status;

import java.time.LocalDateTime;

@Data
@Builder
public class SubmissionSummaryResponse {
    private Integer id;
    private Status status;
    private String stderr;
    private LocalDateTime createdAt;
}
