package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transfers")
@Getter
@Setter
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "from_card_id")
    private Card from;

    @ManyToOne(optional = false)
    @JoinColumn(name = "to_card_id")
    private Card to;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private Instant createdAt;
}
