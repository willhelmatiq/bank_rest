package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.dto.TransferResponseDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.enums.CardStatusCode;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.util.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.Optional;

import static com.example.bankcards.enums.TransferDirection.OUTGOING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceImplTest {

    @Mock
    CardRepository cardRepository;

    @Mock
    TransferRepository transferRepository;

    @InjectMocks
    TransferServiceImpl transferService;

    // ---------- HAPPY PATH ----------
    @Test
    void transfer_success() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user1");

        Card from = TestDataFactory.card(
                1L, "user1", CardStatusCode.ACTIVE, new BigDecimal("100.00")
        );
        Card to = TestDataFactory.card(
                2L, "user1", CardStatusCode.ACTIVE, new BigDecimal("50.00")
        );

        when(cardRepository.findByIdAndUserUsername(1L, "user1"))
                .thenReturn(Optional.of(from));
        when(cardRepository.findByIdAndUserUsername(2L, "user1"))
                .thenReturn(Optional.of(to));

        TransferRequestDto dto =
                new TransferRequestDto(1L, 2L, new BigDecimal("30.00"));

        TransferResponseDto response =
                transferService.transfer(dto, auth);

        assertEquals(OUTGOING, response.direction());
        assertEquals(new BigDecimal("70.00"), from.getBalance());
        assertEquals(new BigDecimal("80.00"), to.getBalance());

        verify(transferRepository).save(any());
    }

    // ---------- SAME CARD ----------
    @Test
    void transfer_sameCard_throwsException() {
        Authentication auth = mock(Authentication.class);

        TransferRequestDto dto =
                new TransferRequestDto(1L, 1L, BigDecimal.TEN);

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> transferService.transfer(dto, auth)
        );

        assertEquals("Cannot transfer money to the same card", ex.getMessage());
    }

    // ---------- SOURCE NOT FOUND ----------
    @Test
    void transfer_sourceCardNotFound() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user1");

        when(cardRepository.findByIdAndUserUsername(1L, "user1"))
                .thenReturn(Optional.empty());

        TransferRequestDto dto =
                new TransferRequestDto(1L, 2L, BigDecimal.TEN);

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> transferService.transfer(dto, auth)
        );

        assertEquals("Source card not found", ex.getMessage());
    }

    // ---------- TARGET NOT FOUND ----------
    @Test
    void transfer_targetCardNotFound() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user1");

        Card from = TestDataFactory.card(
                1L, "user1", CardStatusCode.ACTIVE, BigDecimal.TEN
        );

        when(cardRepository.findByIdAndUserUsername(1L, "user1"))
                .thenReturn(Optional.of(from));
        when(cardRepository.findByIdAndUserUsername(2L, "user1"))
                .thenReturn(Optional.empty());

        TransferRequestDto dto =
                new TransferRequestDto(1L, 2L, BigDecimal.ONE);

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> transferService.transfer(dto, auth)
        );

        assertEquals("Target card not found", ex.getMessage());
    }

    // ---------- CARD NOT ACTIVE ----------
    @Test
    void transfer_inactiveCard() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user1");

        Card from = TestDataFactory.card(
                1L, "user1", CardStatusCode.BLOCKED, BigDecimal.TEN
        );
        Card to = TestDataFactory.card(
                2L, "user1", CardStatusCode.ACTIVE, BigDecimal.TEN
        );

        when(cardRepository.findByIdAndUserUsername(1L, "user1"))
                .thenReturn(Optional.of(from));
        when(cardRepository.findByIdAndUserUsername(2L, "user1"))
                .thenReturn(Optional.of(to));

        TransferRequestDto dto =
                new TransferRequestDto(1L, 2L, BigDecimal.ONE);

        ConflictException ex = assertThrows(
                ConflictException.class,
                () -> transferService.transfer(dto, auth)
        );

        assertEquals("Only ACTIVE cards can be used for transfer", ex.getMessage());
    }

    // ---------- INSUFFICIENT FUNDS ----------
    @Test
    void transfer_insufficientFunds() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user1");

        Card from = TestDataFactory.card(
                1L, "user1", CardStatusCode.ACTIVE, BigDecimal.ONE
        );
        Card to = TestDataFactory.card(
                2L, "user1", CardStatusCode.ACTIVE, BigDecimal.TEN
        );

        when(cardRepository.findByIdAndUserUsername(1L, "user1"))
                .thenReturn(Optional.of(from));
        when(cardRepository.findByIdAndUserUsername(2L, "user1"))
                .thenReturn(Optional.of(to));

        TransferRequestDto dto =
                new TransferRequestDto(1L, 2L, BigDecimal.TEN);

        ConflictException ex = assertThrows(
                ConflictException.class,
                () -> transferService.transfer(dto, auth)
        );

        assertEquals("Insufficient balance", ex.getMessage());
    }
}