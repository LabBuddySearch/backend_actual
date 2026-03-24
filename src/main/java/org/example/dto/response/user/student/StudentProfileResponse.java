package org.example.dto.response.user.student;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StudentProfileResponse {
    private String email;

    private String fullName;

    private String group;

    private StudentProfileSubmission[] submissions;
}
