package org.example.dto.response.user.auth;

import lombok.Builder;
import lombok.Data;
import org.example.entity.Role;

@Data
@Builder
public class UserResponse {
    private Integer id;

    private String email;

    private String fullName;

    private Role role;
}
