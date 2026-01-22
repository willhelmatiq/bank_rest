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

/**
 * REST-контроллер для управления переводами между банковскими картами.
 *
 * <p>Позволяет авторизованному пользователю:</p>
 * <ul>
 *     <li>совершать переводы на карту;</li>
 *     <li>просматривать историю выполненных переводов.</li>
 * </ul>
 *
 * <p>Доступ разрешён только пользователям с ролью {@code USER}.
 * Аутентифицированный пользователь определяется через {@link Authentication}.</p>
 *
 * <p>Вся бизнес-логика переводов инкапсулирована в {@link TransferService}.</p>
 */
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
