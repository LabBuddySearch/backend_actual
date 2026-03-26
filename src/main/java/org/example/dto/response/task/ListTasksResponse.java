package org.example.dto.response.task;

import lombok.Builder;
import lombok.Data;
import org.example.dto.response.user.student.StudentMainTask;

@Data
@Builder
public class ListTasksResponse {
    private StudentMainTask[] tasks;
}
