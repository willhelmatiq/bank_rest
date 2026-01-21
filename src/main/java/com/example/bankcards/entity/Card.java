package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cards")
@Getter
@Setter
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String encryptedNumber;

    @Column(nullable = false)
    private String maskedNumber;

    @Column(nullable = false)
    private String ownerName;

    @Column(nullable = false)
    private LocalDate expirationDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "status_id")
    private CardStatus status;

    @Column(nullable = false)
    private BigDecimal balance;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;
}
