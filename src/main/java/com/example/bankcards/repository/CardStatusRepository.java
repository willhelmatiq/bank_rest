package com.example.bankcards.repository;

import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.enums.CardStatusCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardStatusRepository extends JpaRepository<CardStatus, Long> {
    Optional<CardStatus> findByStatusCode(CardStatusCode statusCode);
}
