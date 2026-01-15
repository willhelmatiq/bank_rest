package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.dto.TransferResponseDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.enums.CardStatusCode;
import com.example.bankcards.exception.BusinessException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static com.example.bankcards.enums.TransferDirection.INCOMING;
import static com.example.bankcards.enums.TransferDirection.OUTGOING;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final CardRepository cardRepository;
    private final TransferRepository transferRepository;

    @Transactional
    public TransferResponseDto transfer(
            TransferRequestDto request,
            Authentication authentication
    ) {
        String username = getUsername(authentication);

        if (request.fromCardId().equals(request.toCardId())) {
            throw new BusinessException("Cannot transfer money to the same card");
        }

        Card from = cardRepository.findByIdAndUserUsername(request.fromCardId(), username)
                .orElseThrow(() -> new BusinessException("Source card not found"));

        Card to = cardRepository.findByIdAndUserUsername(request.toCardId(), username)
                .orElseThrow(() -> new BusinessException("Source card not found"));

        validateCards(from, to, request);

        from.setBalance(from.getBalance().subtract(request.amount()));
        to.setBalance(to.getBalance().add(request.amount()));

        Transfer transfer = new Transfer();
        transfer.setFrom(from);
        transfer.setTo(to);
        transfer.setAmount(request.amount());
        transfer.setCreatedAt(Instant.now());

        transferRepository.save(transfer);

        return mapToDto(transfer, username);
    }

    public List<TransferResponseDto> getUserTransfers(Authentication authentication) {
        String username = getUsername(authentication);

        return transferRepository
                .findAllByUser(username)
                .stream()
                .map(transfer -> mapToDto(transfer, username))
                .toList();
    }

    private void validateCards(Card from, Card to, TransferRequestDto request) {
        if (from.getStatus().getStatusCode() != CardStatusCode.ACTIVE ||
                to.getStatus().getStatusCode() != CardStatusCode.ACTIVE) {
            throw new BusinessException("Only ACTIVE cards can be used for transfer");
        }

        if (from.getBalance().compareTo(request.amount()) < 0) {
            throw new BusinessException("Insufficient balance");
        }
    }

    private TransferResponseDto mapToDto(Transfer transfer, String username) {
        boolean outgoing = transfer.getFrom().getUser().getUsername().equals(username);

        return new TransferResponseDto(
                transfer.getId(),
                transfer.getFrom().getId(),
                transfer.getTo().getId(),
                transfer.getAmount(),
                transfer.getCreatedAt(),
                outgoing ? OUTGOING : INCOMING
        );
    }

    private String getUsername(Authentication auth) {
        return auth.getName();
    }
}