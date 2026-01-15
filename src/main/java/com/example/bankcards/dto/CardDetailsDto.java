package com.example.bankcards.dto;

import com.example.bankcards.enums.CardStatusCode;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardDetailsDto(
        Long id,
        String maskedNumber,
        String ownerName,
        LocalDate expirationDate,
        CardStatusCode status,
        BigDecimal balance
) {}
