package com.thesnellai.luckydna.services;

import com.thesnellai.luckydna.models.User;
import com.thesnellai.luckydna.models.RefreshToken;
import com.thesnellai.luckydna.repositories.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository repository;

    public RefreshTokenService(RefreshTokenRepository repository) {
        this.repository = repository;
    }

    public String create(User user) {
        var refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(Instant.now().plusSeconds(60L * 60 * 24 * 30));

        return repository.save(refreshToken).getToken();
    }

    public RefreshToken validate(String token) {
        var refreshToken = repository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token."));

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            repository.delete(refreshToken);
            throw new IllegalArgumentException("Refresh token expired.");
        }

        return refreshToken;
    }

    @Transactional
    public void revoke(String token) {
        repository.deleteByToken(token);
    }
}