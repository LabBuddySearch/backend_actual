package org.example.execution.executor;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JavaExecutor implements CodeExecutor {

    private static final Pattern PRINTLN_STRING = Pattern.compile(
            "System\\.out\\.println\\s*\\(\\s*\"([^\"]*)\"\\s*\\)"
    );

    @Override
    public ExecutionResult execute(String sourceCode, String inputData, int timeoutMs, int memoryMb) {
        if (sourceCode == null || sourceCode.isBlank()) {
            return compilationError("Пустой исходный код.");
        }

        String trimmed = sourceCode.trim();
        if (!trimmed.contains("class")) {
            return compilationError("Ожидается объявление класса (например, public class Main).");
        }

        if (trimmed.contains("// SYNTAX_ERROR")) {
            return compilationError("Синтаксическая ошибка: неверная конструкция (демо-проверка).");
        }

        if (countChar(trimmed, '{') != countChar(trimmed, '}')) {
            return compilationError("Синтаксическая ошибка: несбалансированные фигурные скобки.");
        }

        String stdout = extractPrintlnOutput(trimmed);
        if (stdout == null) {
            stdout = inputData != null ? inputData.trim() : "";
        }

        return ExecutionResult.builder()
                .stdout(stdout)
                .stderr("")
                .exitCode(0)
                .status("SUCCESS")
                .executionTimeMs(50)
                .build();
    }

    private ExecutionResult compilationError(String message) {
        return ExecutionResult.builder()
                .stdout("")
                .stderr(message)
                .exitCode(1)
                .status("COMPILATION_ERROR")
                .executionTimeMs(0)
                .build();
    }

    private String extractPrintlnOutput(String code) {
        Matcher matcher = PRINTLN_STRING.matcher(code);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private int countChar(String s, char c) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                count++;
            }
        }
        return count;
    }

    @Override
    public String getLanguage() {
        return "JAVA";
    }
}
