package org.example.dto.response.user.teacher;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Teacher group data")
public class TeacherGroup {
    @Schema(
            description = "Group name",
            example = "БПИ-2301"
    )
    private String name;
}
