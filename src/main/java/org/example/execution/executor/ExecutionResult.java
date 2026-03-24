package org.example.execution.executor;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExecutionResult {
    private String stdout;        // То, что программа вывела в консоль
    private String stderr;        // Ошибки компиляции или рантайма
    private int exitCode;         // 0 - успех, остальное - ошибка
    private long executionTimeMs; // Сколько реально работал код
    private String status;        // SUCCESS, TIMEOUT, MEMORY_LIMIT, COMPILATION_ERROR
}
