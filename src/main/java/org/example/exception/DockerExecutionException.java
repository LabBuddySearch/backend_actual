package org.example.exception;

public class DockerExecutionException extends CodeGuardException {
    public DockerExecutionException(String message) {
        super(message, "EXECUTION_ENGINE_ERROR");
    }
}
