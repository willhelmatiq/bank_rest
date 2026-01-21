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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminCardServiceImplTest {

    @Mock
    CardRepository cardRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    CardStatusRepository cardStatusRepository;

    @InjectMocks
    AdminCardServiceImpl adminCardService;

    // ---------- CREATE ----------
    @Test
    void create_success() {
        User user = TestDataFactory.user(1L, "user1");
        CardStatus activeStatus = TestDataFactory.cardStatus(CardStatusCode.ACTIVE);

        CreateCardRequestDto dto = new CreateCardRequestDto(
                1L,
                "Ivan Ivanov",
                "4111111111111111",
                LocalDate.now().plusYears(2),
                new BigDecimal("100.00")
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardStatusRepository.findByStatusCode(CardStatusCode.ACTIVE))
                .thenReturn(Optional.of(activeStatus));

        CardResponseDto response = adminCardService.create(dto);

        assertEquals("Ivan Ivanov", response.ownerName());
        assertEquals(CardStatusCode.ACTIVE, response.status());
        assertEquals(new BigDecimal("100.00"), response.balance());
        assertTrue(response.maskedNumber().endsWith("1111"));

        verify(cardRepository).save(any(Card.class));
    }

    // ---------- CREATE  WHEN USER NOT FOUND----------
    @Test
    void create_userNotFound() {
        CreateCardRequestDto dto = new CreateCardRequestDto(
                99L,
                "Ivan Ivanov",
                "4111111111111111",
                LocalDate.now().plusYears(2),
                BigDecimal.TEN
        );

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> adminCardService.create(dto)
        );

        assertEquals("User not found", ex.getMessage());
    }

    // ---------- BLOCK ----------
    @Test
    void block_success() {
        Card card = TestDataFactory.card(
                1L, "user1", CardStatusCode.ACTIVE, BigDecimal.TEN
        );

        CardStatus blockedStatus = TestDataFactory.cardStatus(CardStatusCode.BLOCKED);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardStatusRepository.findByStatusCode(CardStatusCode.BLOCKED))
                .thenReturn(Optional.of(blockedStatus));

        adminCardService.block(1L);

        assertEquals(CardStatusCode.BLOCKED, card.getStatus().getStatusCode());
    }

    // ---------- BLOCK  WHEN CARD NOT FOUND----------
    @Test
    void block_cardNotFound() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> adminCardService.block(1L)
        );

        assertEquals("Card not found", ex.getMessage());
    }


    // ---------- ACTIVATE ----------
    @Test
    void activate_success() {
        Card card = TestDataFactory.card(
                1L, "user1", CardStatusCode.BLOCKED, BigDecimal.TEN
        );

        CardStatus activeStatus = TestDataFactory.cardStatus(CardStatusCode.ACTIVE);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardStatusRepository.findByStatusCode(CardStatusCode.ACTIVE))
                .thenReturn(Optional.of(activeStatus));

        adminCardService.activate(1L);

        assertEquals(CardStatusCode.ACTIVE, card.getStatus().getStatusCode());
    }

    // ---------- DELETE ----------
    @Test
    void delete_shouldSetStatusClosed() {
        Card card = TestDataFactory.card(
                1L, "user1", CardStatusCode.ACTIVE, BigDecimal.TEN
        );

        CardStatus closedStatus = TestDataFactory.cardStatus(CardStatusCode.CLOSED);

        when(cardRepository.findById(1L))
                .thenReturn(Optional.of(card));

        when(cardStatusRepository.findByStatusCode(CardStatusCode.CLOSED))
                .thenReturn(Optional.of(closedStatus));

        adminCardService.delete(1L);

        assertEquals(CardStatusCode.CLOSED, card.getStatus().getStatusCode());

        verify(cardRepository, never()).deleteById(any());
    }

    // ---------- GET ALL ----------
    @Test
    void getAll_success() {
        Card card1 = TestDataFactory.card(
                1L, "user1", CardStatusCode.ACTIVE, BigDecimal.TEN
        );
        Card card2 = TestDataFactory.card(
                2L, "user2", CardStatusCode.BLOCKED, BigDecimal.ONE
        );

        Page<Card> page = new PageImpl<>(
                List.of(card1, card2),
                PageRequest.of(0, 10),
                2
        );

        when(cardRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        Page<CardResponseDto> result =
                adminCardService.getAll(PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
        assertEquals(CardStatusCode.ACTIVE, result.getContent().get(0).status());
        assertEquals(CardStatusCode.BLOCKED, result.getContent().get(1).status());
    }
}