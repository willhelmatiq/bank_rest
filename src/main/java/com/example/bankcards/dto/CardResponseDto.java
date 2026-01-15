package com.example.bankcards.dto;

import com.example.bankcards.enums.CardStatusCode;

import java.math.BigDecimal;

public record CardResponseDto(
        Long id,
        String maskedNumber,
        CardStatusCode status,
        BigDecimal balance
) {}
