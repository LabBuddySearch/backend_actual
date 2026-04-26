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
        if (result.getExitCode() != 0) {
            if (result.getStderr() != null && result.getStderr().contains("error:")) {
                return Status.COMPILATION_ERROR;
            }
            return Status.RUNTIME_ERROR;
        }
        if (!normalize(result.getStdout()).equals(normalize(expectedOutput))) {
            return Status.WRONG_ANSWER;
        }
        return Status.ACCEPTED;
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

