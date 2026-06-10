package com.example.demo.services;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.demo.dtos.AuthResponse;

@Service
public class TokenService {

    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, TokenRecord> accessTokens = new ConcurrentHashMap<>();
    private final Map<String, TokenRecord> refreshTokens = new ConcurrentHashMap<>();
    private final Duration accessTokenDuration;
    private final Duration refreshTokenDuration;

    public TokenService(
        @Value("${app.auth.access-token-minutes}") long accessTokenMinutes,
        @Value("${app.auth.refresh-token-days}") long refreshTokenDays
    ) {
        this.accessTokenDuration = Duration.ofMinutes(accessTokenMinutes);
        this.refreshTokenDuration = Duration.ofDays(refreshTokenDays);
    }

    public AuthResponse issueTokens(Long studentId) {
        Instant now = Instant.now();
        String accessToken = generateToken();
        String refreshToken = generateToken();
        Instant accessTokenExpiresAt = now.plus(accessTokenDuration);
        Instant refreshTokenExpiresAt = now.plus(refreshTokenDuration);

        accessTokens.put(accessToken, new TokenRecord(studentId, accessTokenExpiresAt));
        refreshTokens.put(refreshToken, new TokenRecord(studentId, refreshTokenExpiresAt));

        return new AuthResponse(studentId, accessToken, accessTokenExpiresAt, refreshToken, refreshTokenExpiresAt);
    }

    public Optional<Long> validateAccessToken(String accessToken) {
        return validateToken(accessTokens, accessToken);
    }

    public Optional<AuthResponse> refreshAccessToken(String refreshToken) {
        return validateToken(refreshTokens, refreshToken)
            .map(this::issueTokens);
    }

    private Optional<Long> validateToken(Map<String, TokenRecord> tokens, String token) {
        TokenRecord tokenRecord = tokens.get(token);
        if (tokenRecord == null) {
            return Optional.empty();
        }

        if (Instant.now().isAfter(tokenRecord.expiresAt())) {
            tokens.remove(token);
            return Optional.empty();
        }

        return Optional.of(tokenRecord.studentId());
    }

    private String generateToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private record TokenRecord(Long studentId, Instant expiresAt) {
    }
}
