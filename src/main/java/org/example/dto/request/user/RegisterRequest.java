package org.example.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class RegisterRequest {
    private Role role;

    @NotBlank
    @Size(max = 255)
    private String fullName;

    @Email
    @Size(max = 255)
    @NotBlank
    private String email;

    @NotBlank
    @Size(max = 255)
    private String password;

    @Size(max = 100)
    private String student_group;
}
