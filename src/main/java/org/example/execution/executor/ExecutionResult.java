package org.example.execution.executor;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExecutionResult {
    private String stdout;
    private String stderr;
    private int exitCode;
    private long executionTimeMs;
    private String status;
    private boolean timedOut;
}
