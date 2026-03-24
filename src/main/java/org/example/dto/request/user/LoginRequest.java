package org.example.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {
    @Email
    private String email;

    @NotBlank
    @Size(max = 255)
    private String password;
}
