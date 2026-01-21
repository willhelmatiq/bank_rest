package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.dto.TransferResponseDto;
import com.example.bankcards.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/transfers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public TransferResponseDto transfer(
            @RequestBody @Valid TransferRequestDto request,
            Authentication authentication
    ) {
        return transferService.transfer(request, authentication);
    }

    @GetMapping
    public List<TransferResponseDto> history(Authentication authentication) {
        return transferService.getUserTransfers(authentication);
    }
}
