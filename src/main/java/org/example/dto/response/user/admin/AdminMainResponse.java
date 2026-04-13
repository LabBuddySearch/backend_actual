package org.example.dto.response.user.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Admin data on the main page")
public class AdminMainResponse {
    @Schema(
            description = "Total student count",
            example = "100"
    )
    private int studentCount;

    @Schema(
            description = "Total teacher count",
            example = "20"
    )
    private int teacherCount;

    @Schema(
            description = "Total task count",
            example = "60"
    )
    private int taskCount;


}
