package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.dto.TransferResponseDto;
import com.example.bankcards.enums.TransferDirection;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.service.TransferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = TransferController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransferService transferService;

    // ---------- POST /api/user/transfers ----------
    @Test
    void transfer_success() throws Exception {

        TransferRequestDto request = new TransferRequestDto(
                1L,
                2L,
                new BigDecimal("10.50")
        );

        TransferResponseDto response = new TransferResponseDto(
                100L,
                1L,
                2L,
                new BigDecimal("10.50"),
                Instant.parse("2026-01-20T00:00:00Z"),
                TransferDirection.OUTGOING
        );

        when(transferService.transfer(any(), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/user/transfers")
                        .contentType("application/json")
                        .content("""
                                {
                                  "fromCardId": 1,
                                  "toCardId": 2,
                                  "amount": 10.50
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.fromCardId").value(1))
                .andExpect(jsonPath("$.toCardId").value(2))
                .andExpect(jsonPath("$.amount").value(10.50))
                .andExpect(jsonPath("$.direction").value("OUTGOING"));
    }

    // ---------- POST /api/user/transfers (validation) ----------
    @Test
    void transfer_invalidAmount_returns400() throws Exception {

        mockMvc.perform(post("/api/user/transfers")
                        .contentType("application/json")
                        .content("""
                                {
                                  "fromCardId": 1,
                                  "toCardId": 2,
                                  "amount": 0
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    // ---------- POST /api/user/transfers (business error) ----------
    @Test
    void transfer_businessException_returns400() throws Exception {

        when(transferService.transfer(any(), any()))
                .thenThrow(new BadRequestException("Insufficient balance"));

        mockMvc.perform(post("/api/user/transfers")
                        .contentType("application/json")
                        .content("""
                                {
                                  "fromCardId": 1,
                                  "toCardId": 2,
                                  "amount": 50
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient balance"));
    }

    // ---------- GET /api/user/transfers ----------
    @Test
    void history_success() throws Exception {

        TransferResponseDto transfer = new TransferResponseDto(
                101L,
                1L,
                2L,
                new BigDecimal("20.00"),
                Instant.parse("2026-01-20T01:00:00Z"),
                TransferDirection.INCOMING
        );

        when(transferService.getUserTransfers(any()))
                .thenReturn(List.of(transfer));

        mockMvc.perform(get("/api/user/transfers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(101))
                .andExpect(jsonPath("$[0].direction").value("INCOMING"));
    }
}
