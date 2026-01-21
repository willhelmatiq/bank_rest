package com.example.bankcards.controller;

import com.example.bankcards.dto.BalanceResponseDto;
import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.enums.CardStatusCode;
import com.example.bankcards.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/cards")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class UserCardController {

    private final CardService cardService;

    @GetMapping
    public Page<CardResponseDto> getMyCards(
            Pageable pageable,
            Authentication authentication
    ) {
        return cardService.getUserCards(authentication, pageable);
    }

    @GetMapping("/status/{status}")
    public Page<CardResponseDto> getMyCardsByStatus(
            @PathVariable CardStatusCode status,
            Pageable pageable,
            Authentication authentication
    ) {
        return cardService.getUserCardsByStatus(authentication, status, pageable);
    }

    @GetMapping("/{id}/balance")
    public BalanceResponseDto getBalance(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return cardService.getBalance(id, authentication);
    }

    @PostMapping("/{id}/block-request")
    public void requestBlock(
            @PathVariable Long id,
            Authentication authentication
    ) {
        cardService.requestBlock(id, authentication);
    }
}