package org.example.dto.request.group;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Create a new study group")
public class CreateGroupRequest {
    @NotBlank
    @Size(max = 100)
    @Schema(description = "Group code / name", example = "БПИ-2301")
    private String name;
}
