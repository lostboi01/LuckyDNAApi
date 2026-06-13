package com.thesnellai.luckydna.dto;

public record ResultCheckResponse(
        Integer whiteBallMatches,
        Boolean powerBallMatch,
        String prizeTier
) {
}