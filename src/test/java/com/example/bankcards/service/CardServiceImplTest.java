package com.example.bankcards.service;

import com.example.bankcards.dto.BalanceResponseDto;
import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.enums.CardStatusCode;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.exception.ForbiddenException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.CardStatusRepository;
import com.example.bankcards.util.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    CardRepository cardRepository;

    @Mock
    CardStatusRepository cardStatusRepository;

    @InjectMocks
    CardServiceImpl cardService;


    // ---------- GET USER CARDS ----------
    @Test
    void getUserCards_success() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user1");

        Card card1 = TestDataFactory.card(1L, "user1", CardStatusCode.ACTIVE, BigDecimal.TEN);
        Card card2 = TestDataFactory.card(2L, "user1", CardStatusCode.BLOCKED, BigDecimal.ONE);

        Page<Card> page = new PageImpl<>(
                List.of(card1, card2),
                PageRequest.of(0, 10),
                2
        );

        when(cardRepository.findAllByUser_UsernameAndStatus_StatusCodeNot(
                eq("user1"),
                eq(CardStatusCode.CLOSED),
                any(Pageable.class)
        )).thenReturn(page);

        Page<CardResponseDto> result =
                cardService.getUserCards(auth, PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
        assertEquals(CardStatusCode.ACTIVE, result.getContent().get(0).status());
        assertEquals(CardStatusCode.BLOCKED, result.getContent().get(1).status());

        verify(cardRepository).findAllByUser_UsernameAndStatus_StatusCodeNot(
                eq("user1"),
                eq(CardStatusCode.CLOSED),
                any(Pageable.class)
        );
    }

    // ---------- GET USER CARDS BY STATUS ----------
    @Test
    void getUserCardsByStatus_success() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user1");

        Card card = TestDataFactory.card(
                1L, "user1", CardStatusCode.ACTIVE, BigDecimal.TEN
        );

        Page<Card> page = new PageImpl<>(List.of(card));

        when(cardRepository.findAllByUser_UsernameAndStatus_StatusCode(
                eq("user1"),
                eq(CardStatusCode.ACTIVE),
                any(Pageable.class)
        )).thenReturn(page);

        Page<CardResponseDto> result =
                cardService.getUserCardsByStatus(
                        auth,
                        CardStatusCode.ACTIVE,
                        PageRequest.of(0, 10)
                );

        assertEquals(1, result.getTotalElements());
        assertEquals(CardStatusCode.ACTIVE, result.getContent().getFirst().status());
    }

    // ---------- GET USER CARDS BY STATUS CLOSED ----------
    @Test
    void getUserCardsByStatus_closed_throwsException() {
        Authentication auth = mock(Authentication.class);

        ForbiddenException ex = assertThrows(
                ForbiddenException.class,
                () -> cardService.getUserCardsByStatus(
                        auth,
                        CardStatusCode.CLOSED,
                        PageRequest.of(0, 10)
                )
        );

        assertEquals("Closed cards are not accessible for user", ex.getMessage());
        verifyNoInteractions(cardRepository);
    }

    // ---------- GET BALANCE ----------
    @Test
    void getBalance_success() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user1");

        Card card = TestDataFactory.card(
                1L, "user1", CardStatusCode.ACTIVE, new BigDecimal("123.45")
        );

        when(cardRepository.findByIdAndUser_UsernameAndStatus_StatusCodeNot(1L, "user1", CardStatusCode.CLOSED))
                .thenReturn(Optional.of(card));

        BalanceResponseDto response =
                cardService.getBalance(1L, auth);

        assertEquals(1L, response.cardId());
        assertEquals(new BigDecimal("123.45"), response.balance());
    }

    // ---------- GET USER CARDS NOT FOUND ----------
    @Test
    void getBalance_cardNotFound() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user1");

        when(cardRepository.findByIdAndUser_UsernameAndStatus_StatusCodeNot(1L, "user1", CardStatusCode.CLOSED))
                .thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> cardService.getBalance(1L, auth)
        );

        assertEquals("Card not found", ex.getMessage());
    }

    // ---------- REQUEST BLOCK ----------
    @Test
    void requestBlock_success() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user1");

        Card card = TestDataFactory.card(1L, "user1", CardStatusCode.ACTIVE, BigDecimal.TEN);

        CardStatus blockedStatus = new CardStatus();
        blockedStatus.setStatusCode(CardStatusCode.BLOCKED);

        when(cardRepository.findByIdAndUser_UsernameAndStatus_StatusCodeNot(1L, "user1", CardStatusCode.CLOSED))
                .thenReturn(Optional.of(card));
        when(cardStatusRepository.findByStatusCode(CardStatusCode.BLOCKED))
                .thenReturn(Optional.of(blockedStatus));

        cardService.requestBlock(1L, auth);

        assertEquals(CardStatusCode.BLOCKED, card.getStatus().getStatusCode());
    }

    // ---------- REQUEST BLOCK WHEN CARD ALREADY BLOCKED----------
    @Test
    void requestBlock_cardAlreadyBlocked_throwsException() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user1");

        Card card = TestDataFactory.card(
                1L,
                "user1",
                CardStatusCode.BLOCKED,
                BigDecimal.TEN
        );

        when(cardRepository.findByIdAndUser_UsernameAndStatus_StatusCodeNot(
                1L,
                "user1",
                CardStatusCode.CLOSED
        )).thenReturn(Optional.of(card));

        ConflictException ex = assertThrows(
                ConflictException.class,
                () -> cardService.requestBlock(1L, auth)
        );

        assertEquals("Card is already blocked", ex.getMessage());
        verifyNoInteractions(cardStatusRepository);
    }

    // ---------- REQUEST BLOCK WHEN CARD WAS EXPIRED----------
    @Test
    void requestBlock_expiredCard_throwsException() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user1");

        Card card = TestDataFactory.card(
                1L,
                "user1",
                CardStatusCode.EXPIRED,
                BigDecimal.TEN
        );

        when(cardRepository.findByIdAndUser_UsernameAndStatus_StatusCodeNot(
                1L,
                "user1",
                CardStatusCode.CLOSED
        )).thenReturn(Optional.of(card));

        ConflictException ex = assertThrows(
                ConflictException.class,
                () -> cardService.requestBlock(1L, auth)
        );

        assertEquals("Expired card cannot be blocked", ex.getMessage());

        // 🔒 Статус не должен искаться
        verifyNoInteractions(cardStatusRepository);
    }

    // ---------- REQUEST BLOCK WHEN ALREADY BLOCKED----------
    @Test
    void requestBlock_alreadyBlocked() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user1");

        Card card = TestDataFactory.card(1L, "user1", CardStatusCode.BLOCKED, BigDecimal.TEN);

        when(cardRepository.findByIdAndUser_UsernameAndStatus_StatusCodeNot(1L, "user1", CardStatusCode.CLOSED))
                .thenReturn(Optional.of(card));

        ConflictException ex = assertThrows(
                ConflictException.class,
                () -> cardService.requestBlock(1L, auth)
        );

        assertEquals("Card is already blocked", ex.getMessage());
    }

    // ---------- REQUEST BLOCK WHEN CARD WAS EXPIRED----------
    @Test
    void requestBlock_expiredCard() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user1");

        Card card = TestDataFactory.card(1L, "user1", CardStatusCode.EXPIRED, BigDecimal.TEN);

        when(cardRepository.findByIdAndUser_UsernameAndStatus_StatusCodeNot(1L, "user1", CardStatusCode.CLOSED))
                .thenReturn(Optional.of(card));

        ConflictException ex = assertThrows(
                ConflictException.class,
                () -> cardService.requestBlock(1L, auth)
        );

        assertEquals("Expired card cannot be blocked", ex.getMessage());
    }
}
