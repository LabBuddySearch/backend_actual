package org.example.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Test data")
public class TestCaseDto {
    @Schema(
            description = "Test input data",
            example = "ABBCABA",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    private String inputData;

    @Schema(
            description = "Expected test output",
            example = "5",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    private String expectedOutput;

    @Schema(
            description = "Can the student see this test ",
            example = "false",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Boolean isHidden;
}
