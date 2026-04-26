package org.example.execution.executor;

import org.example.exception.DockerExecutionException;
import org.springframework.stereotype.Component;

@Component
public class PythonExecutor implements CodeExecutor {
    @Override
    public ExecutionResult execute(String sourceCode, String inputData, int timeoutMs, int memoryMb) {
        throw new DockerExecutionException("Python strategy is not implemented yet");
    }

    @Override
    public String getLanguage() {
        return "PYTHON";
    }
}

