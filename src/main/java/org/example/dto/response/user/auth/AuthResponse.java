package org.example.dto.response.user.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
@Schema(description = "Authentication data")
public class AuthResponse {
    @Schema(
            description = "User access token",
            example = "fkadsj31242q31kl"
    )
    private String accessToken;

    @Builder.Default
    @Schema(
            description = "User token type",
            example = "Bearer"
    )
    private String tokenType = "Bearer";

    @Schema(
            description = "User token expiration time in milliseconds",
            example = "1321231"
    )
    private long expiresInMs;

    @Schema(
            description = "User data",
            example = """
                    [
                        {
                            "id": "312312123",
                            "email": "user@example.com",
                            "fullName": "Иван Иванов",
                            "role": "STUDENT"
                        }
                    ]
                    """
    )
    private UserResponse user;
}
