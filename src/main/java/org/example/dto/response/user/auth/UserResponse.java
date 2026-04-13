package org.example.dto.response.user.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.example.entity.Role;

@Data
@Builder
public class UserResponse {
    @Schema(
            description = "User ID",
            example = "312131"
    )
    private Integer id;

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
            description = "User role",
            example = "STUDENT"
    )
    private Role role;
}
