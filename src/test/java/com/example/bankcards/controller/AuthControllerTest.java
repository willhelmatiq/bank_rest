package com.example.bankcards.controller;

import com.example.bankcards.dto.JwtResponseDto;
import com.example.bankcards.exception.ForbiddenException;
import com.example.bankcards.exception.UnauthorizedException;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.service.AuthService;
import org.junit.jupiter.api.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    // ---------- SUCCESS ----------
    @Test
    void login_success() throws Exception {

        JwtResponseDto response =
                new JwtResponseDto("jwt-token-123");

        when(authService.login(any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content("""
                                {
                                  "username": "user1",
                                  "password": "password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-123"));
    }

    // ---------- BAD CREDENTIALS ----------
    @Test
    void login_invalidCredentials() throws Exception {

        when(authService.login(any()))
                .thenThrow(new UnauthorizedException(
                        "Invalid username or password"
                ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content("""
                                {
                                  "username": "user1",
                                  "password": "wrong"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Invalid username or password"));
    }

    // ---------- BLOCKED USER ----------
    @Test
    void login_userBlocked() throws Exception {

        when(authService.login(any()))
                .thenThrow(new ForbiddenException(
                        "User has been blocked"
                ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content("""
                                {
                                  "username": "user1",
                                  "password": "password"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value("User has been blocked"));
    }

    // ---------- VALIDATION ----------
    @Test
    void login_validationError() throws Exception {

        mockMvc.perform(post("/api/auth/login")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content("""
                                {
                                  "username": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
