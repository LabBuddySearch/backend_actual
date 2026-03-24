package org.example.dto.response.user.student;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentTestCase {
    private String inputData;

    private String expectedOutput;
}
