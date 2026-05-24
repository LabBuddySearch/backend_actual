package org.example.dto.response.user.teacher;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeacherGroupStudentResponse {
    private Integer id;
    private String fullName;
    private String email;
}
