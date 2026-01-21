package com.example.bankcards.service;

import com.example.bankcards.dto.JwtResponseDto;
import com.example.bankcards.dto.LoginRequestDto;
import com.example.bankcards.exception.BusinessException;
import com.example.bankcards.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    AuthService authService;

    // ---------- HAPPY PATH ----------
    @Test
    void login_success() {
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(jwtTokenProvider.generateToken(authentication))
                .thenReturn("jwt-token");

        JwtResponseDto response = authService.login(
                new LoginRequestDto("user1", "password")
        );

        assertEquals("jwt-token", response.token());

        verify(authenticationManager).authenticate(any());
        verify(jwtTokenProvider).generateToken(authentication);
    }

    // ---------- USER BLOCKED ----------
    @Test
    void login_userBlocked_throwsForbidden() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new DisabledException("User disabled"));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> authService.login(
                        new LoginRequestDto("user1", "password")
                )
        );

        assertEquals("User has been blocked", ex.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());

        verify(authenticationManager).authenticate(any());
        verifyNoInteractions(jwtTokenProvider);
    }

    // ---------- BAD CREDENTIALS ----------
    @Test
    void login_badCredentials_throwsUnauthorized() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> authService.login(
                        new LoginRequestDto("user1", "wrong-password")
                )
        );

        assertEquals("Invalid username or password", ex.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());

        verify(authenticationManager).authenticate(any());
        verifyNoInteractions(jwtTokenProvider);
    }
}
