package com.example.bankcards.service;

import com.example.bankcards.dto.UserResponseDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.exception.ForbiddenException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    AdminUserServiceImpl adminUserService;

    // ---------- GET ALL USERS----------
    @Test
    void getAll_success() {
        Role userRole = TestDataFactory.role("USER");
        User user1 = TestDataFactory.user(1L, "user1", userRole, true);
        User user2 = TestDataFactory.user(2L, "user2", userRole, true);

        Page<User> page = new PageImpl<>(List.of(user1, user2));

        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        Page<UserResponseDto> result = adminUserService.getAll(PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
        UserResponseDto dto = result.getContent().getFirst();
        assertEquals("user1", dto.username());
        assertEquals("USER", dto.role());
        assertTrue(dto.enabled());
    }

    // ---------- BLOCK SUCCESS ----------
    @Test
    void block_success() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin");

        Role adminRole = TestDataFactory.role("ADMIN");
        Role userRole = TestDataFactory.role("USER");
        User admin = TestDataFactory.user(1L, "admin", adminRole, true);
        User user = TestDataFactory.user(2L, "user1", userRole, true);

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(admin));
        when(userRepository.findById(2L))
                .thenReturn(Optional.of(user));

        adminUserService.block(2L, auth);

        assertFalse(user.isEnabled());
    }

    // ---------- BLOCK ALREADY BLOCKED ----------
    @Test
    void block_userAlreadyBlocked() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin");

        Role adminRole = TestDataFactory.role("ADMIN");
        Role userRole = TestDataFactory.role("USER");
        User admin = TestDataFactory.user(1L, "admin", adminRole, true);
        User user = TestDataFactory.user(2L, "user1", userRole, false);

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(admin));
        when(userRepository.findById(2L))
                .thenReturn(Optional.of(user));

        ConflictException ex = assertThrows(
                ConflictException.class,
                () -> adminUserService.block(2L, auth)
        );

        assertEquals("User already blocked", ex.getMessage());
    }

    // ---------- BLOCK SELF ----------
    @Test
    void block_self() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin");

        Role adminRole = TestDataFactory.role("ADMIN");
        User admin = TestDataFactory.user(1L, "admin", adminRole, true);

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(admin));
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(admin));

        ForbiddenException ex = assertThrows(
                ForbiddenException.class,
                () -> adminUserService.block(1L, auth)
        );

        assertEquals("Admin cannot block himself", ex.getMessage());
    }

    // ---------- BLOCK ANOTHER ADMIN ----------
    @Test
    void block_anotherAdmin() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin1");

        Role adminRole = TestDataFactory.role("ADMIN");
        User admin1 = TestDataFactory.user(1L, "admin1", adminRole, true);
        User admin2 = TestDataFactory.user(2L, "admin2", adminRole, true);

        when(userRepository.findByUsername("admin1"))
                .thenReturn(Optional.of(admin1));
        when(userRepository.findById(2L))
                .thenReturn(Optional.of(admin2));

        ForbiddenException ex = assertThrows(
                ForbiddenException.class,
                () -> adminUserService.block(2L, auth)
        );

        assertEquals("Admin cannot block another admin", ex.getMessage());
    }

    // ---------- UNBLOCK SUCCESS ----------
    @Test
    void unblock_success() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin");

        Role adminRole = TestDataFactory.role("ADMIN");
        Role userRole = TestDataFactory.role("USER");
        User admin = TestDataFactory.user(1L, "admin", adminRole, true);
        User user = TestDataFactory.user(2L, "user1", userRole, false);

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(admin));
        when(userRepository.findById(2L))
                .thenReturn(Optional.of(user));

        adminUserService.unblock(2L, auth);

        assertTrue(user.isEnabled());
    }

    // ---------- UNBLOCK ALREADY ACTIVE ----------
    @Test
    void unblock_userAlreadyActive() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin");

        Role adminRole = TestDataFactory.role("ADMIN");
        Role userRole = TestDataFactory.role("USER");
        User admin = TestDataFactory.user(1L, "admin", adminRole, true);
        User user = TestDataFactory.user(2L, "user1", userRole, true);

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(admin));
        when(userRepository.findById(2L))
                .thenReturn(Optional.of(user));

        ConflictException ex = assertThrows(
                ConflictException.class,
                () -> adminUserService.unblock(2L, auth)
        );

        assertEquals("User already active", ex.getMessage());
    }

    // ---------- UNBLOCK SELF ----------
    @Test
    void unblock_self() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin");

        Role adminRole = TestDataFactory.role("ADMIN");
        User admin = TestDataFactory.user(1L, "admin", adminRole, false);

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(admin));
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(admin));

        ForbiddenException ex = assertThrows(
                ForbiddenException.class,
                () -> adminUserService.unblock(1L, auth)
        );

        assertEquals("Admin cannot unblock himself", ex.getMessage());
    }

    // ---------- UNBLOCK ANOTHER ADMIN ----------
    @Test
    void unblock_anotherAdmin() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin1");

        Role adminRole = TestDataFactory.role("ADMIN");
        User admin1 = TestDataFactory.user(1L, "admin1", adminRole, true);
        User admin2 = TestDataFactory.user(2L, "admin2", adminRole, false);

        when(userRepository.findByUsername("admin1"))
                .thenReturn(Optional.of(admin1));
        when(userRepository.findById(2L))
                .thenReturn(Optional.of(admin2));

        ForbiddenException ex = assertThrows(
                ForbiddenException.class,
                () -> adminUserService.unblock(2L, auth)
        );

        assertEquals("Admin cannot unblock another admin", ex.getMessage());
    }
}
