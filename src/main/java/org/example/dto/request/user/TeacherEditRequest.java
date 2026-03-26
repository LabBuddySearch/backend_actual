package org.example.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TeacherEditRequest {
    @Email
    @Size(max = 255)
    private String email;

    @Size(max = 255)
    private String fullName;

    private int age;

    @Size(max = 20)
    private String phoneNumber;

}
