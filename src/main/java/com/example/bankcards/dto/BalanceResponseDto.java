package com.example.bankcards.dto;

import java.math.BigDecimal;

public record BalanceResponseDto (
        Long cardId,
        BigDecimal balance
) {}
