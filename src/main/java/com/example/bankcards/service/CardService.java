package com.example.bankcards.service;

import com.example.bankcards.dto.BalanceResponseDto;
import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.enums.CardStatusCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

public interface CardService {

    Page<CardResponseDto> getUserCards(
            Authentication authentication,
            Pageable pageable
    );

    Page<CardResponseDto> getUserCardsByStatus(
            Authentication authentication,
            CardStatusCode status,
            Pageable pageable
    );

    BalanceResponseDto getBalance(
            Long cardId,
            Authentication authentication
    );

    void requestBlock(
            Long cardId,
            Authentication authentication
    );
}
