package org.example.dto.response.user.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminMainResponse {
    private int studentCount;

    private int teacherCount;

    private int taskCount;


}
