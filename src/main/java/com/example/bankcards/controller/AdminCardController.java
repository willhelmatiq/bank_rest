package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.dto.CreateCardRequestDto;
import com.example.bankcards.service.AdminCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/cards")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCardController {

    private final AdminCardService adminCardService;

    @PostMapping
    public CardResponseDto create(@RequestBody @Valid CreateCardRequestDto dto) {
        return adminCardService.create(dto);
    }

    @PatchMapping("/{id}/block")
    public void block(@PathVariable Long id) {
        adminCardService.block(id);
    }

    @PatchMapping("/{id}/activate")
    public void activate(@PathVariable Long id) {
        adminCardService.activate(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        adminCardService.delete(id);
    }

    @GetMapping
    public Page<CardResponseDto> getAll(Pageable pageable) {
        return adminCardService.getAll(pageable);
    }
}
