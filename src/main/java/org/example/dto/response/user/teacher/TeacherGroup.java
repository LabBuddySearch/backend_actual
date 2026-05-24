package org.example.dto.response.user.teacher;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Teacher group data")
public class TeacherGroup {
    @Schema(description = "Group ID", example = "1")
    private Integer id;

    @Schema(
            description = "Group name",
            example = "БПИ-2301"
    )
    private String name;

    private java.util.List<TeacherGroupStudentResponse> students;
}
