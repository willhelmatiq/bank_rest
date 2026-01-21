package com.example.bankcards.controller;

import com.example.bankcards.WebConfig;
import com.example.bankcards.dto.BalanceResponseDto;
import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.enums.CardStatusCode;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.service.CardService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = UserCardController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@Import(WebConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class UserCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    // ---------- GET /api/user/cards ----------
    @Test
    void getMyCards_success() throws Exception {
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

        when(cardService.getUserCards(any(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/user/cards")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    // ---------- GET /api/user/cards/status/{status} ----------
    @Test
    void getMyCardsByStatus_success() throws Exception {

        CardResponseDto card = new CardResponseDto(
                1L,
                "**** **** **** 1234",
                "USER ONE",
                LocalDate.of(2028, 12, 31),
                CardStatusCode.BLOCKED,
                new BigDecimal("50.00")
        );

        Page<CardResponseDto> page =
                new PageImpl<>(List.of(card));

        when(cardService.getUserCardsByStatus(any(), eq(CardStatusCode.BLOCKED), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/user/cards/status/BLOCKED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("BLOCKED"));
    }

    // ---------- GET /api/user/cards/{id}/balance ----------
    @Test
    void getBalance_success() throws Exception {

        BalanceResponseDto balance =
                new BalanceResponseDto(1L, new BigDecimal("123.45"));

        when(cardService.getBalance(eq(1L), any()))
                .thenReturn(balance);

        mockMvc.perform(get("/api/user/cards/1/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardId").value(1))
                .andExpect(jsonPath("$.balance").value(123.45));
    }

    // ---------- POST /api/user/cards/{id}/block-request ----------
    @Test
    void requestBlock_success() throws Exception {

        doNothing().when(cardService).requestBlock(eq(1L), any());

        mockMvc.perform(post("/api/user/cards/1/block-request"))
                .andExpect(status().isOk());

        verify(cardService).requestBlock(eq(1L), any());
    }
}
