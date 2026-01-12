package com.example.bankcards.entity;

import com.example.bankcards.enums.CardStatusCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "card_statuses")
@Getter
@Setter
public class CardStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private CardStatusCode statusCode;
}
