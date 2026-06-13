package com.thesnellai.luckydna.dto;

import java.time.LocalDate;

public record ResultCheckRequest(
        LocalDate drawingDate,
        Integer whiteBall1,
        Integer whiteBall2,
        Integer whiteBall3,
        Integer whiteBall4,
        Integer whiteBall5,
        Integer powerBall
) {
}