package org.example.dto.response.user.student;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentContextResponse {
    private String groupName;
    private String teacherName;
    private String teacherEmail;
}
