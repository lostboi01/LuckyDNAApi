package com.thesnellai.luckydna.dto;

import com.thesnellai.luckydna.arenas.ArenaCode;

import java.time.LocalDate;

public record SavePlayRequest(
        ArenaCode arenaCode,
        LocalDate playDate,
        int whiteBall1,
        int whiteBall2,
        int whiteBall3,
        int whiteBall4,
        int whiteBall5,
        int powerBall
) {
}