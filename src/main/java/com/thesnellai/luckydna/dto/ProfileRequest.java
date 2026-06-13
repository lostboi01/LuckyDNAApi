package com.thesnellai.luckydna.dto;

import java.time.LocalDate;

public record ProfileRequest(
        LocalDate birthday,
        String favoriteNumbers,
        String luckyAttributes
) {}
