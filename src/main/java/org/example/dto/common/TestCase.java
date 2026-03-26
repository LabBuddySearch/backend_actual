package org.example.dto.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestCase {
    private String inputData;

    private String expectedOutput;
}
