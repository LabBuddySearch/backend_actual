package org.example.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentEditRequest {
    @Email
    @Size(max = 255)
    private String email;

    @Size(max = 255)
    private String fullName;

    private int age;

    @Size(max = 100)
    private String group;

    @Size(max = 20)
    private String phoneNumber;

}
