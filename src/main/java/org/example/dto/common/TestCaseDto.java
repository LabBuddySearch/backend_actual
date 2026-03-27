package org.example.dto.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestCaseDto {
    private String inputData;

    private String expectedOutput;

    private Boolean isHidden;
}
