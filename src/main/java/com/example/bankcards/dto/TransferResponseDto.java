package com.example.bankcards.dto;

import com.example.bankcards.enums.TransferDirection;

import java.math.BigDecimal;
import java.time.Instant;

public record TransferResponseDto(
        Long id,
        Long fromCardId,
        Long toCardId,
        BigDecimal amount,
        Instant createdAt,
        TransferDirection direction
) {}