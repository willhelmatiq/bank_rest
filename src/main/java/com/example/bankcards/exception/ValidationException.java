package com.example.bankcards.exception;

public class ValidationException extends ApiException {
    public ValidationException(String message) {
        super(message);
    }
}
