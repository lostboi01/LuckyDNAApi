package com.thesnellai.luckydna.dto;

import java.util.List;

public record CheckAllResultsResponse(
        Integer savedPlaysChecked,
        Integer drawingsChecked,
        Integer resultsCreated,
        Integer winningResults,
        Integer jackpots,
        Integer bestWhiteBallMatch,
        List<ResultSummary> winners
) {
    public record ResultSummary(
            Long resultId,
            Long savedPlayId,
            Long drawingId,
            String drawingDate,
            Integer whiteBallMatches,
            Boolean powerBallMatch,
            String prizeTier
    ) {}
}