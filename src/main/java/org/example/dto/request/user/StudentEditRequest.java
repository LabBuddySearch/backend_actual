package org.example.dto.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Schema(description = "Edit student profile data")
public class StudentEditRequest {
    @Email
    @Size(max = 255)
    @Schema(
            description = "User email",
            example = "user@example.com"
    )
    private String email;

    @Schema(
            description = "User full name",
            example = "Иван Иванов"
    )
    @Size(max = 255)
    private String fullName;

    @Schema(
            description = "User age",
            example = "20"
    )
    private int age;

    @Schema(
            description = "User student group",
            example = "БПИ-2301"
    )
    @Size(max = 100)
    private String group;

    @Schema(
            description = "User phone number",
            example = "89999999999"
    )
    @Size(max = 20)
    private String phoneNumber;

}
