package org.example.execution.service;

import org.example.entity.Status;
import org.example.execution.executor.ExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExecutionResultParserServiceTest {

    private ExecutionResultParserService parser;

    @BeforeEach
    void setUp() {
        parser = new ExecutionResultParserService();
    }

    @Test
    void timedOut_mapsToTle() {
        ExecutionResult r = ExecutionResult.builder()
                .timedOut(true)
                .exitCode(124)
                .stdout("")
                .stderr("Time limit exceeded")
                .build();
        assertEquals(Status.TIME_LIMIT_EXCEEDED, parser.resolveStatus(r, "x"));
    }

    @Test
    void pythonSyntax_mapsToCompilationError() {
        ExecutionResult r = ExecutionResult.builder()
                .timedOut(false)
                .exitCode(1)
                .stdout("")
                .stderr("SyntaxError: invalid syntax")
                .status("SYNTAX_ERROR")
                .build();
        assertEquals(Status.COMPILATION_ERROR, parser.resolveStatus(r, ""));
    }

    @Test
    void javaCompile_mapsByStatus() {
        ExecutionResult r = ExecutionResult.builder()
                .timedOut(false)
                .exitCode(1)
                .stdout("")
                .stderr("Main.java:3: error: cannot find symbol")
                .status("COMPILE_ERROR")
                .build();
        assertEquals(Status.COMPILATION_ERROR, parser.resolveStatus(r, ""));
    }

    @Test
    void javaCompile_mapsByStderr() {
        ExecutionResult r = ExecutionResult.builder()
                .timedOut(false)
                .exitCode(1)
                .stdout("")
                .stderr("Main.java:3: error: cannot find symbol")
                .status("FAILED")
                .build();
        assertEquals(Status.COMPILATION_ERROR, parser.resolveStatus(r, ""));
    }

    @Test
    void runtimeError_maps() {
        ExecutionResult r = ExecutionResult.builder()
                .timedOut(false)
                .exitCode(1)
                .stdout("")
                .stderr("Exception in thread \"main\" java.lang.NullPointerException")
                .status("RUNTIME_ERROR")
                .build();
        assertEquals(Status.RUNTIME_ERROR, parser.resolveStatus(r, ""));
    }

    @Test
    void acceptedWhenOutputMatches() {
        ExecutionResult r = ExecutionResult.builder()
                .timedOut(false)
                .exitCode(0)
                .stdout("42\n")
                .stderr("")
                .status("SUCCESS")
                .build();
        assertEquals(Status.ACCEPTED, parser.resolveStatus(r, "42"));
    }

    @Test
    void wrongAnswerWhenOutputDiffers() {
        ExecutionResult r = ExecutionResult.builder()
                .timedOut(false)
                .exitCode(0)
                .stdout("41")
                .stderr("")
                .status("SUCCESS")
                .build();
        assertEquals(Status.WRONG_ANSWER, parser.resolveStatus(r, "42"));
    }

    @Test
    void normalizesLineEndingsAndTrim() {
        ExecutionResult r = ExecutionResult.builder()
                .timedOut(false)
                .exitCode(0)
                .stdout("  hello\r\n")
                .stderr("")
                .status("SUCCESS")
                .build();
        assertEquals(Status.ACCEPTED, parser.resolveStatus(r, "hello"));
    }
}
