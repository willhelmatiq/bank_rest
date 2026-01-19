package com.example.bankcards.util;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.enums.CardStatusCode;

import java.math.BigDecimal;

public class TestDataFactory {

    public static Card card(
            Long id,
            String username,
            CardStatusCode status,
            BigDecimal balance
    ) {
        User user = new User();
        user.setUsername(username);

        CardStatus cardStatus = new CardStatus();
        cardStatus.setStatusCode(status);

        Card card = new Card();
        card.setId(id);
        card.setUser(user);
        card.setStatus(cardStatus);
        card.setBalance(balance);

        return card;
    }
}
