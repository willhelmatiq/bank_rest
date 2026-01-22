package com.example.bankcards.exception;

public abstract class ApiException extends RuntimeException {

    protected ApiException(String message) {
        super(message);
    }
}
