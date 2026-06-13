package com.thesnellai.luckydna.dto;

import java.math.BigDecimal;
import java.util.List;

public record ResultsStatisticsResponse(
        Integer totalResultsChecked,
        Integer totalWins,
        Integer jackpots,
        Integer bestWhiteBallMatch,
        Integer powerBallMatches,
        BigDecimal totalPrizeValue,
        List<WinnerSummary> recentWinners
) {
    public record WinnerSummary(
            Long resultId,
            Long savedPlayId,
            Long drawingId,
            String drawingDate,
            Integer whiteBallMatches,
            Boolean powerBallMatch,
            String prizeTier,
            BigDecimal prizeAmount
    ) {}
}