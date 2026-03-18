package org.example.exception;

public class DockerExecutionException extends RuntimeException {
    public DockerExecutionException(String message) {
        super(message);
    }
}
