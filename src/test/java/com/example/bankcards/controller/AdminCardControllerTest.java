package com.example.bankcards.controller;

import com.example.bankcards.WebConfig;
import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.dto.CreateCardRequestDto;
import com.example.bankcards.enums.CardStatusCode;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.service.AdminCardService;
import org.junit.jupiter.api.MediaType;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AdminCardController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@Import(WebConfig.class)
@AutoConfigureMockMvc
class AdminCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminCardService adminCardService;

    // ---------- POST /api/admin/cards ----------
    @Test
    void create_success() throws Exception {

        CreateCardRequestDto request = new CreateCardRequestDto(
                1L,
                "Ivan Ivanov",
                "4111111111111234",
                LocalDate.of(2028, 12, 31),
                new BigDecimal("100.00")
        );

        CardResponseDto response = new CardResponseDto(
                1L,
                "**** **** **** 1234",
                "Ivan Ivanov",
                LocalDate.of(2028, 12, 31),
                CardStatusCode.ACTIVE,
                new BigDecimal("0.00")
        );

        when(adminCardService.create(any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/admin/cards")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content("""
                                {
                                    "userId": 1,
                                    "ownerName": "Ivan Ivanov",
                                    "cardNumber": "4111111111111234",
                                    "expirationDate": "2028-12-31",
                                    "balance": 100.00
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    // ---------- PATCH /api/admin/cards/{id}/block ----------
    @Test
    void block_success() throws Exception {

        doNothing().when(adminCardService).block(1L);

        mockMvc.perform(patch("/api/admin/cards/1/block"))
                .andExpect(status().isOk());

        verify(adminCardService).block(1L);
    }

    // ---------- PATCH /api/admin/cards/{id}/activate ----------
    @Test
    void activate_success() throws Exception {

        doNothing().when(adminCardService).activate(1L);

        mockMvc.perform(patch("/api/admin/cards/1/activate"))
                .andExpect(status().isOk());

        verify(adminCardService).activate(1L);
    }

    // ---------- DELETE /api/admin/cards/{id} ----------
    @Test
    void delete_success() throws Exception {

        doNothing().when(adminCardService).delete(1L);

        mockMvc.perform(delete("/api/admin/cards/1"))
                .andExpect(status().isOk());

        verify(adminCardService).delete(1L);
    }

    // ---------- GET /api/admin/cards ----------
    @Test
    void getAll_success() throws Exception {

        CardResponseDto card = new CardResponseDto(
                1L,
                "**** **** **** 1234",
                "USER ONE",
                LocalDate.of(2028, 12, 31),
                CardStatusCode.ACTIVE,
                new BigDecimal("100.00")
        );

        Page<CardResponseDto> page =
                new PageImpl<>(List.of(card), PageRequest.of(0, 10), 1);

        when(adminCardService.getAll(any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/admin/cards")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }
}