package com.example.bankcards.exception;

import com.example.bankcards.dto.ErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDto> handleBusiness(
            BusinessException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                ex.getStatus(),
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    // ---------- BUILDER ----------
    private ResponseEntity<ErrorResponseDto> buildResponse(
            HttpStatus status,
            String message,
            String path,
            Map<String, String> validationErrors
    ) {
        return ResponseEntity
                .status(status)
                .body(new ErrorResponseDto(
                        Instant.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        message,
                        path,
                        validationErrors
                ));
    }
}

