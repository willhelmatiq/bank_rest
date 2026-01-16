package com.example.bankcards.service;

import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.dto.CreateCardRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminCardService {

    CardResponseDto create(CreateCardRequestDto dto);

    void block(Long cardId);

    void activate(Long cardId);

    void delete(Long cardId);

    Page<CardResponseDto> getAll(Pageable pageable);
}
