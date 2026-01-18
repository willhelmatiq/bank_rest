package com.example.bankcards.service;

import com.example.bankcards.dto.UserResponseDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BusinessException;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAll(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::mapToDto);
    }

    @Override
    @Transactional
    public void block(Long userId, Authentication authentication) {
        User currentAdmin = userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new BusinessException("User not found"));

        User targetUser = getUser(userId);

        if (!targetUser.isEnabled()) {
            throw new BusinessException("User already blocked");
        }

        if (currentAdmin.getId().equals(targetUser.getId())) {
            throw new BusinessException("Admin cannot block himself");
        }

        if (targetUser.getRole().getTitle().equals("ADMIN")) {
            throw new BusinessException("Admin cannot block another admin");
        }

        targetUser.setEnabled(false);
    }

    @Override
    @Transactional
    public void unblock(Long userId, Authentication authentication) {
        User currentAdmin = userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new BusinessException("User not found"));

        User targetUser = getUser(userId);

        if (targetUser.isEnabled()) {
            throw new BusinessException("User already active");
        }

        if (currentAdmin.getId().equals(targetUser.getId())) {
            throw new BusinessException("Admin cannot unblock himself");
        }

        if (targetUser.getRole().getTitle().equals("ADMIN")) {
            throw new BusinessException("Admin cannot unblock another admin");
        }

        targetUser.setEnabled(true);
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found"));
    }

    private UserResponseDto mapToDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getRole().getTitle(),
                user.isEnabled()
        );
    }
}
