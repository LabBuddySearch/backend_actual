package org.example.exception;

import lombok.Getter;

@Getter
public class CodeGuardException extends RuntimeException {
    private final String errorCode;

    public CodeGuardException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
