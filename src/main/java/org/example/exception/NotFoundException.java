package org.example.exception;

import java.util.UUID;

public class NotFoundException extends ApiException {
    public NotFoundException(String message) {
        super(message, "OBJECT_NOT_FOUND");
    }
}
