package org.example.dto.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Change user status")
public class ChangeStatusRequest {
    @NotNull
    @Schema(
            description = "User ID",
            example = "123234132",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long id;
}
