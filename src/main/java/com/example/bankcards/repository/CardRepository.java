package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.enums.CardStatusCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    Page<Card> findAllByUserUsername(
            String username,
            Pageable pageable
    );

    Page<Card> findAllByUser_UsernameAndStatus_StatusCode(
            String username,
            CardStatusCode status,
            Pageable pageable
    );

    Optional<Card> findByIdAndUserUsername(Long id, String username);
}
