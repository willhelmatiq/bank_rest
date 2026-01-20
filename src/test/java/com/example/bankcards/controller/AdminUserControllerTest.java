package com.example.bankcards.controller;

import com.example.bankcards.WebConfig;
import com.example.bankcards.dto.UserResponseDto;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.service.AdminUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AdminUserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@Import(WebConfig.class)
@AutoConfigureMockMvc
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminUserService adminUserService;

    // ---------- GET /api/admin/users ----------
    @Test
    void getAll_success() throws Exception {

        UserResponseDto user = new UserResponseDto(
                1L,
                "user1",
                "USER",
                true
        );

        Page<UserResponseDto> page =
                new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);

        when(adminUserService.getAll(any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/admin/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    // ---------- POST /api/admin/users/{id}/block ----------
    @Test
    void block_success() throws Exception {

        doNothing().when(adminUserService)
                .block(eq(1L), any(Authentication.class));

        mockMvc.perform(post("/api/admin/users/1/block"))
                .andExpect(status().isOk());

        verify(adminUserService)
                .block(eq(1L), any());
    }

    // ---------- POST /api/admin/users/{id}/unblock ----------
    @Test
    void unblock_success() throws Exception {

        doNothing().when(adminUserService)
                .unblock(eq(1L), any(Authentication.class));

        mockMvc.perform(post("/api/admin/users/1/unblock"))
                .andExpect(status().isOk());

        verify(adminUserService)
                .unblock(eq(1L), any());
    }
}
