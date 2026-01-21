package com.example.bankcards.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferRequestDto(

        @NotNull
        Long fromCardId,

        @NotNull
        Long toCardId,

        @NotNull
        @DecimalMin(value = "0.01", inclusive = true)
        BigDecimal amount
) {
}
