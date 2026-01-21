package com.example.bankcards.service;

import com.example.bankcards.dto.BalanceResponseDto;
import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.enums.CardStatusCode;
import com.example.bankcards.exception.BusinessException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.CardStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final CardStatusRepository cardStatusRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<CardResponseDto> getUserCards(Authentication authentication, Pageable pageable) {
        String username = authentication.getName();
        Page<Card> cards = cardRepository.findAllByUser_UsernameAndStatus_StatusCodeNot(username, CardStatusCode.CLOSED, pageable);
        return cards.map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardResponseDto> getUserCardsByStatus(Authentication authentication, CardStatusCode status, Pageable pageable) {
        if (status == CardStatusCode.CLOSED) {
            throw new BusinessException("Closed cards are not accessible for user");
        }

        String username = authentication.getName();
        Page<Card> cards = cardRepository.findAllByUser_UsernameAndStatus_StatusCode(username, status, pageable);
        return cards.map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public BalanceResponseDto getBalance(Long cardId, Authentication authentication) {
        Card card = getUserCard(cardId, authentication);

        return new BalanceResponseDto(
                card.getId(),
                card.getBalance()
        );
    }

    @Override
    @Transactional
    public void requestBlock(Long cardId, Authentication authentication) {
        Card card = getUserCard(cardId, authentication);

        if (card.getStatus().getStatusCode() == CardStatusCode.BLOCKED) {
            throw new BusinessException("Card is already blocked");
        }

        if (card.getStatus().getStatusCode() == CardStatusCode.EXPIRED) {
            throw new BusinessException("Expired card cannot be blocked");
        }

        CardStatus blockedStatus = cardStatusRepository
                .findByStatusCode(CardStatusCode.BLOCKED)
                .orElseThrow(() -> new IllegalStateException("BLOCKED status not found"));

        card.setStatus(blockedStatus);
    }

    private Card getUserCard(Long cardId, Authentication authentication) {
        return cardRepository
                .findByIdAndUser_UsernameAndStatus_StatusCodeNot(cardId, authentication.getName(), CardStatusCode.CLOSED)
                .orElseThrow(() -> new BusinessException("Card not found"));
    }

    private CardResponseDto mapToDto(Card card) {
        return new CardResponseDto(
                card.getId(),
                card.getMaskedNumber(),
                card.getOwnerName(),
                card.getExpirationDate(),
                card.getStatus().getStatusCode(),
                card.getBalance()
        );
    }
}
