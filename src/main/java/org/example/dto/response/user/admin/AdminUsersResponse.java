package org.example.dto.response.user.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "List of users data")
public class AdminUsersResponse {
    @Schema(
            description = "List users",
            example = """
                    [
                        {
                            "id": "1231231",
                            "fullName": "Иван Иванов",
                            "role": "STUDENT",
                            "status": "ACTIVE'
                        }
                    ]
                    """
    )
    private AdminUser[] users;

}
