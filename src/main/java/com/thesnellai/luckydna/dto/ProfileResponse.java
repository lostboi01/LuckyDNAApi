package com.thesnellai.luckydna.dto;

import java.time.LocalDate;

public record ProfileResponse(
        Long id,
        LocalDate birthday,
        String favoriteNumbers,
        String luckyAttributes
) {}
