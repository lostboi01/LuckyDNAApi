package com.thesnellai.luckydna.dto;

public record AuthResponse(
        String token,
        String refreshToken,
        Long userId,
        String fullName,
        String email
) {}
