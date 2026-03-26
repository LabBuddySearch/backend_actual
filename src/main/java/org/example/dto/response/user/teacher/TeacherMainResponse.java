package org.example.dto.response.user.teacher;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeacherMainResponse {
    private String fullName;

    private TeacherGroup[] groups;
}
