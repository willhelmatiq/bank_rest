package com.example.bankcards.exception;

public class ConflictException extends ApiException{
    public ConflictException(String message) {
        super(message);
    }
}
