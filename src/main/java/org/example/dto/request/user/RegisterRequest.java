package org.example.dto.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
@Schema(description = "User data for registration")
public class RegisterRequest {
    @Schema(
            description = "User role: STUDENT/TEACHER",
            example = "STUDENT",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    private Role role;

    @NotBlank
    @Size(max = 255)
    @Schema(
            description = "User full name",
            example = "Иван Иванов",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String fullName;

    @Email
    @Size(max = 255)
    @NotBlank
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
            example = "pass1234",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String password;

    @Size(max = 100)
    @Schema(
            description = "Student group for user with role STUDENT",
            example = "БПИ-2301",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String student_group;
}
