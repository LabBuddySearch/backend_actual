package org.example.dto.response.user.student;

import lombok.Builder;
import lombok.Data;
import org.example.dto.response.task.ListTasksResponse;

@Data
@Builder
public class StudentMainResponse {
    private String fullName;

    private ListTasksResponse tasks;
}
