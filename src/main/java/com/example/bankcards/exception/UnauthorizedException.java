package com.example.bankcards.exception;

public class UnauthorizedException extends  ApiException{
    public UnauthorizedException(String message) {
        super(message);
    }
}
