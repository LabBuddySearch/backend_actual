package org.example.execution.executor;

import org.example.execution.executor.ExecutionResult;

public interface CodeExecutor {
    ExecutionResult execute(String sourceCode, String inputData, int timeoutMs, int memoryMb);
    String getLanguage();
}