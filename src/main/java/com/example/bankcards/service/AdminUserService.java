package com.example.bankcards.service;

import com.example.bankcards.dto.UserResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

public interface AdminUserService {

    Page<UserResponseDto> getAll(Pageable pageable);

    void block(Long userId, Authentication authentication);

    void unblock(Long userId, Authentication authentication);
}
