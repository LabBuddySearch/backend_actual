package org.example.dto.response.user.teacher;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TeacherGroupsResponse {
    private List<TeacherGroup> groups;
}
