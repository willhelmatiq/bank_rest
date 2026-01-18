package com.example.bankcards.controller;

import com.example.bankcards.dto.UserResponseDto;
import com.example.bankcards.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public Page<UserResponseDto> getAll(Pageable pageable) {
        return adminUserService.getAll(pageable);
    }

    @PostMapping("/{id}/block")
    public void block(@PathVariable Long id, Authentication authentication) {
        adminUserService.block(id, authentication);
    }

    @PostMapping("/{id}/unblock")
    public void unblock(@PathVariable Long id, Authentication authentication) {
        adminUserService.unblock(id, authentication);
    }
}

