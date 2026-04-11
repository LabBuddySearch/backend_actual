package org.example.exception;

// Попытка добавить уже существующий объект (пользователь с email, который есть в БД)
public class NotUniqueObjectException extends ApiException {
    public NotUniqueObjectException(String message) {
        super(message, "OBJECT_NOT_UNIQUE");
    }
}
