package com.example.bankcards.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateCardRequestDto(

        @NotNull
        Long userId,

        @NotBlank
        String ownerName,

        @NotBlank
        @Pattern(regexp = "\\d{16}", message = "Card number must contain 16 digits")
        String cardNumber,

        @NotNull
        @Future
        LocalDate expirationDate,

        @NotNull
        @DecimalMin(value = "0.00", inclusive = true)
        BigDecimal balance
) {}
