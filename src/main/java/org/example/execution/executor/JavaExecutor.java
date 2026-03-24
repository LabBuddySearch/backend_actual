package org.example.execution.executor;


import org.springframework.stereotype.Component;

@Component
public class JavaExecutor implements CodeExecutor {

    @Override
    public ExecutionResult execute(String sourceCode, String inputData, int timeoutMs, int memoryMb) {
        return ExecutionResult.builder()
                .stdout("Код получен сервисом разработке")
                .exitCode(0)
                .status("MOCK_SUCCESS")
                .executionTimeMs(100)
                .build();
    }

    @Override
    public String getLanguage() {
        return "JAVA";
    }
}
