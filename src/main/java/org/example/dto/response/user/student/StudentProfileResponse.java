package org.example.dto.response.user.student;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Student profile page data")
public class StudentProfileResponse {
    @Schema(
            description = "User email",
            example = "user@example.com"
    )
    private String email;

    @Schema(
            description = "User full name",
            example = "Иван Иванов"
    )
    private String fullName;

    @Schema(
            description = "User student group",
            example = "БПИ-2301"
    )
    private String group;

    @Schema(
            description = "User email",
            example = """
                    [
                        {
                            "id": "131231",
                            "status": "WRONG",
                            "language": "Java",
                            "executionTimeMs": "11121"
                        }
                    ]
                    """
    )
    private List<StudentProfileSubmission> submissions;
}
