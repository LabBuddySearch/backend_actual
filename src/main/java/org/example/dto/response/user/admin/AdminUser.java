package org.example.dto.response.user.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminUser {
    private Long id;

    private String fullName;

    private String role;

    private String status;
}
