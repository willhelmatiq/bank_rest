package com.example.bankcards.dto;

public record UserResponseDto(
        Long id,
        String username,
        String role,
        boolean enabled
) {}
