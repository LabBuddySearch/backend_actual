package org.example.dto.request.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentTaskRequest {
    private Long id;

    private String code;
}
