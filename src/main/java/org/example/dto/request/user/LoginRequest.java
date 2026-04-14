package org.example.dto.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Data for login user")
public class LoginRequest {
    @Email
    @Schema(
            description = "User email",
            example = "user@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @NotBlank
    @Size(max = 255)
    @Schema(
            description = "User password",
            example = "1111",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String password;
}
