package org.example.dto.response.user.student;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentMainResponse {
    private String fullName;

    private StudentMainTask[] tasks;
}
