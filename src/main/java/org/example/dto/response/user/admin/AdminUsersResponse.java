package org.example.dto.response.user.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminUsersResponse {
    private AdminUser[] users;

}
