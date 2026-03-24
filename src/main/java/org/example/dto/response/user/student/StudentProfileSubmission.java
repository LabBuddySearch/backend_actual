package org.example.dto.response.user.student;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StudentProfileSubmission {
    private String title;

    private String status;

    private String language;

    private LocalDateTime dateTime;
}
