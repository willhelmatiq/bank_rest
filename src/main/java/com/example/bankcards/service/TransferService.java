package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.dto.TransferResponseDto;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface TransferService {

    TransferResponseDto transfer(TransferRequestDto request, Authentication authentication);

    List<TransferResponseDto> getUserTransfers(Authentication authentication);
}
