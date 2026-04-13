package org.example.dto.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Edit teacher profile data")
public class TeacherEditRequest {
    @Email
    @Size(max = 255)
    @Schema(
            description = "User email",
            example = "user@example.com")
    private String email;

    @Size(max = 255)
    @Schema(
            description = "User full name",
            example = "Иван Иванов"
    )
    private String fullName;

    @Schema(
            description = "User age",
            example = "20"
    )
    private int age;

    @Size(max = 20)
    @Schema(
            description = "User phone number",
            example = "89999999999"
    )
    private String phoneNumber;

}
