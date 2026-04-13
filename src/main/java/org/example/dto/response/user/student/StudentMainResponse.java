package org.example.dto.response.user.student;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.example.dto.response.task.ListTasksResponse;

@Data
@Builder
@Schema(description = "Student main page data")
public class StudentMainResponse {
    @Schema(
            description = "Student full name",
            example = "Иван Иванов"
    )
    private String fullName;

    @Schema(
            description = "Student tasks list",
            example = """
                    [
                        {
                            "id": "31231231",
                            "title": "Быстрая сортировка",
                            "author": "Иван Иванов"
                        }
                    ]
                    """
    )
    private ListTasksResponse tasks;
}
