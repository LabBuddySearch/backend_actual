package org.example.dto.response.user.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Admin data about one user")
public class AdminUser {
    @Schema(
            description = "User ID",
            example = "12312341"
    )
    private Long id;

    @Schema(
            description = "User full name",
            example = "Иван Иванов"
    )
    private String fullName;

    @Schema(
            description = "User role",
            example = "STUDENT"
    )
    private String role;

    @Schema(
            description = "User status",
            example = "ACTIVE"
    )
    private String status;
}
