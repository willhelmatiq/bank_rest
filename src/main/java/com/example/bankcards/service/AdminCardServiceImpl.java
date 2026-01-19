package com.example.bankcards.service;

import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.dto.CreateCardRequestDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.enums.CardStatusCode;
import com.example.bankcards.exception.BusinessException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.CardStatusRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardCryptoUtil;
import com.example.bankcards.util.CardMaskUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminCardServiceImpl implements AdminCardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardStatusRepository cardStatusRepository;

    @Override
    @Transactional
    public CardResponseDto create(CreateCardRequestDto dto) {

        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new BusinessException("User not found"));

        CardStatus activeStatus = cardStatusRepository
                .findByStatusCode(CardStatusCode.ACTIVE)
                .orElseThrow();

        Card card = new Card();
        card.setUser(user);
        card.setOwnerName(dto.ownerName());
        card.setExpirationDate(dto.expirationDate());
        card.setBalance(dto.balance());
        card.setStatus(activeStatus);
        card.setEncryptedNumber(CardCryptoUtil.encrypt(dto.cardNumber()));
        card.setMaskedNumber(CardMaskUtil.mask(dto.cardNumber()));

        cardRepository.save(card);

        return mapToDto(card);
    }

    @Override
    @Transactional
    public void block(Long cardId) {
        Card card = getCard(cardId);
        CardStatus blockedStatus = cardStatusRepository
                .findByStatusCode(CardStatusCode.BLOCKED)
                .orElseThrow(() -> new IllegalStateException("BLOCKED status not found"));
        card.setStatus(blockedStatus);
    }

    @Override
    @Transactional
    public void activate(Long cardId) {
        Card card = getCard(cardId);
        CardStatus activeStatus = cardStatusRepository
                .findByStatusCode(CardStatusCode.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("ACTIVE status not found"));

        card.setStatus(activeStatus);
    }

    @Override
    @Transactional
    public void delete(Long cardId) {
        Card card = getCard(cardId);

        CardStatus closedStatus = cardStatusRepository
                .findByStatusCode(CardStatusCode.CLOSED)
                .orElseThrow(() -> new IllegalStateException("CLOSED status not found"));

        card.setStatus(closedStatus);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardResponseDto> getAll(Pageable pageable) {
        return cardRepository.findAll(pageable)
                .map(this::mapToDto);
    }

    private Card getCard(Long cardId) {
        return cardRepository
                .findById(cardId)
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
