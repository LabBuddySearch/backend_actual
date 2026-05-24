package org.example.execution.service;

import org.example.dto.response.task.SubmissionResponse;
import org.example.entity.Status;
import org.example.execution.executor.ExecutionResult;
import org.springframework.stereotype.Service;

@Service
public class ExecutionResultParserService {

    public Status resolveStatus(ExecutionResult result, String expectedOutput) {
        if (result.isTimedOut()) {
            return Status.TIME_LIMIT_EXCEEDED;
        }
        String execStatus = result.getStatus() == null ? "" : result.getStatus();
        if ("COMPILE_ERROR".equals(execStatus) || "SYNTAX_ERROR".equals(execStatus)) {
            return Status.COMPILATION_ERROR;
        }
        if (result.getExitCode() != 0) {
            if ("MEMORY_LIMIT_EXCEEDED".equals(execStatus)) {
                return Status.RUNTIME_ERROR;
            }
            if (looksLikeJavaCompileError(result.getStderr())) {
                return Status.COMPILATION_ERROR;
            }
            return Status.RUNTIME_ERROR;
        }
        if (!normalize(result.getStdout()).equals(normalize(expectedOutput))) {
            return Status.WRONG_ANSWER;
        }
        return Status.ACCEPTED;
    }

    private boolean looksLikeJavaCompileError(String stderr) {
        if (stderr == null || stderr.isBlank()) {
            return false;
        }
        return stderr.contains("error:") && !stderr.toLowerCase().contains("exception in thread");
    }

    public void enrichResponse(SubmissionResponse response, ExecutionResult result) {
        response.setStdout(result.getStdout());
        response.setStderr(result.getStderr());
    }

    private String normalize(String output) {
        if (output == null) {
            return "";
        }
        return output.replace("\r\n", "\n").trim();
    }
}

