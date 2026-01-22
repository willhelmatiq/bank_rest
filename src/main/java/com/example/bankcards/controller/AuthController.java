package com.example.bankcards.controller;

import com.example.bankcards.dto.JwtResponseDto;
import com.example.bankcards.dto.LoginRequestDto;
import com.example.bankcards.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-контроллер аутентификации пользователей.
 *
 * <p>Предоставляет API для входа пользователя в систему
 * и получения JWT-токена.</p>
 *
 * <p>Контроллер не требует аутентификации и используется
 * для первичного входа в систему.</p>
 *
 * <p>Вся бизнес-логика аутентификации и генерации токена
 * инкапсулирована в {@link AuthService}.</p>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public JwtResponseDto login(@RequestBody @Valid LoginRequestDto request) {
        return authService.login(request);
    }
}
