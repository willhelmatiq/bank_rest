package com.example.bankcards.config;

import com.example.bankcards.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Основная конфигурация безопасности приложения.
 *
 * <p>Настраивает:</p>
 * <ul>
 *   <li>JWT-аутентификацию без использования HTTP-сессий (stateless)</li>
 *   <li>Spring Security Filter Chain</li>
 *   <li>CORS-политику для взаимодействия с фронтендом</li>
 *   <li>Хэширование паролей с использованием BCrypt</li>
 * </ul>
 *
 * <p>Все запросы, кроме публичных эндпоинтов аутентификации и документации,
 * требуют валидного JWT-токена.</p>
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    /**
     * Конфигурирует цепочку фильтров Spring Security.
     *
     * <p>Отключает CSRF (т.к. используется stateless JWT),
     * включает CORS, настраивает правила доступа и
     * регистрирует JWT-фильтр перед стандартной аутентификацией.</p>
     *
     * @param http объект {@link HttpSecurity}
     * @return сконфигурированная {@link SecurityFilterChain}
     * @throws Exception в случае ошибок конфигурации
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .sessionManagement(s ->
                        s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**", "/docs/**")
                                .permitAll()
                                .anyRequest()
                                .authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * Предоставляет {@link AuthenticationManager},
     * используемый при аутентификации пользователя.
     *
     * @param config стандартная конфигурация Spring Security
     * @return {@link AuthenticationManager}
     * @throws Exception в случае ошибок инициализации
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Конфигурация CORS для разрешения запросов с фронтенд-приложений.
     *
     * <p>Список разрешённых origin'ов задаётся через
     * параметр {@code app.cors.allowed-origins} в конфигурации приложения.</p>
     *
     * @param allowedOrigins список разрешённых источников
     * @return {@link CorsConfigurationSource}
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins}") List<String> allowedOrigins) {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
