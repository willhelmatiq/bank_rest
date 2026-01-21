package com.example.bankcards.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CardMaskUtil {

    public String mask(String cardNumber) {
        if (cardNumber == null || cardNumber.length() != 16) {
            throw new IllegalArgumentException("Invalid card number");
        }

        return "**** **** **** " + cardNumber.substring(12);
    }
}
