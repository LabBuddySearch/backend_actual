package org.example.dto.response.user.teacher;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Teacher main page data")
public class TeacherMainResponse {
    @Schema(
            description = "Teacher full name",
            example = "Иван Иванов"
    )
    private String fullName;

    @Schema(
            description = "List of groups",
            example = """
                    [
                        {
                            'name': 'БПИ-2301'
                        }
                    ]
                    """
    )
    private List<TeacherGroup> groups;
}
