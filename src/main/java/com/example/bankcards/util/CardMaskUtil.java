package com.example.bankcards.util;

import lombok.experimental.UtilityClass;

/**
 * Utility класс для маскирования номера банковской карты.
 *
 * <p>Используется при формировании ответов API для скрытия
 * полного номера карты и предотвращения утечки
 * чувствительных данных.</p>
 *
 * <p>Оставляет видимыми только последние четыре цифры номера карты
 * в формате {@code **** **** **** 1234}.</p>
 */
@UtilityClass
public class CardMaskUtil {

    public String mask(String cardNumber) {
        if (cardNumber == null || cardNumber.length() != 16) {
            throw new IllegalArgumentException("Invalid card number");
        }

        return "**** **** **** " + cardNumber.substring(12);
    }
}
