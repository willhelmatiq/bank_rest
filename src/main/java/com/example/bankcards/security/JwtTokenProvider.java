package com.example.bankcards.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Компонент для генерации и валидации JWT-токенов.
 *
 * <p>Отвечает за:</p>
 * <ul>
 *     <li>создание JWT-токена на основе аутентифицированного пользователя;</li>
 *     <li>проверку валидности токена;</li>
 *     <li>извлечение имени пользователя из токена.</li>
 * </ul>
 *
 * <p>Токен подписывается симметричным ключом (HMAC),
 * секрет и время жизни токена задаются через конфигурацию приложения.</p>
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.expiration}")
    private long expiration;

    public String generateToken(Authentication authentication) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(authentication.getName())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getKey())
                .compact();
    }

    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }
}
