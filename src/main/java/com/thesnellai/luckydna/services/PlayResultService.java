package com.thesnellai.luckydna.services;

import com.thesnellai.luckydna.dto.CheckAllResultsResponse;
import com.thesnellai.luckydna.dto.ResultsStatisticsResponse;
import com.thesnellai.luckydna.models.*;
import com.thesnellai.luckydna.repositories.PlayResultRepository;
import com.thesnellai.luckydna.repositories.PowerBallDrawingRepository;
import com.thesnellai.luckydna.repositories.SavedPlayRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PlayResultService {
    private final SavedPlayRepository savedPlayRepository;
    private final PowerBallDrawingRepository drawingRepository;
    private final PlayResultRepository playResultRepository;

    public PlayResultService(
            SavedPlayRepository savedPlayRepository,
            PowerBallDrawingRepository drawingRepository,
            PlayResultRepository playResultRepository
    ) {
        this.savedPlayRepository = savedPlayRepository;
        this.drawingRepository = drawingRepository;
        this.playResultRepository = playResultRepository;
    }

    public CheckAllResultsResponse checkAll(User user) {
        var plays = savedPlayRepository.findByUserOrderByCreatedAtDesc(user);
        var drawings = drawingRepository.findAll();

        int created = 0;

        for (var play : plays) {
            for (var drawing : drawings) {
                var existing = playResultRepository
                        .findByUserAndSavedPlayAndDrawing(user, play, drawing);

                if (existing.isPresent()) {
                    continue;
                }

                var result = calculateResult(user, play, drawing);
                playResultRepository.save(result);
                created++;
            }
        }

        var allResults = playResultRepository.findByUserOrderByCheckedAtDesc(user);

        var winners = allResults.stream()
                .filter(result -> !"No Prize".equals(result.getPrizeTier()))
                .map(result -> new CheckAllResultsResponse.ResultSummary(
                        result.getId(),
                        result.getSavedPlay().getId(),
                        result.getDrawing().getId(),
                        result.getDrawing().getDrawingDate().toString(),
                        result.getWhiteBallMatches(),
                        result.getPowerBallMatch(),
                        result.getPrizeTier()
                ))
                .toList();

        int jackpots = (int) allResults.stream()
                .filter(result -> "Jackpot".equals(result.getPrizeTier()))
                .count();

        int bestWhiteMatch = allResults.stream()
                .mapToInt(PlayResult::getWhiteBallMatches)
                .max()
                .orElse(0);

        return new CheckAllResultsResponse(
                plays.size(),
                drawings.size(),
                created,
                winners.size(),
                jackpots,
                bestWhiteMatch,
                winners
        );
    }

    private PlayResult calculateResult(
            User user,
            SavedPlay play,
            PowerBallDrawing drawing
    ) {
        Set<Integer> playBalls = new HashSet<>(List.of(
                play.getWhiteBall1(),
                play.getWhiteBall2(),
                play.getWhiteBall3(),
                play.getWhiteBall4(),
                play.getWhiteBall5()
        ));

        Set<Integer> drawingBalls = new HashSet<>(List.of(
                drawing.getWhiteBall1(),
                drawing.getWhiteBall2(),
                drawing.getWhiteBall3(),
                drawing.getWhiteBall4(),
                drawing.getWhiteBall5()
        ));

        playBalls.retainAll(drawingBalls);

        int whiteMatches = playBalls.size();
        boolean powerBallMatch = drawing.getPowerBall().equals(play.getPowerBall());

        var result = new PlayResult();
        result.setUser(user);
        result.setPrizeAmount(determinePrizeAmount(whiteMatches, powerBallMatch));
        result.setSavedPlay(play);
        result.setDrawing(drawing);
        result.setWhiteBallMatches(whiteMatches);
        result.setPowerBallMatch(powerBallMatch);
        result.setPrizeTier(determinePrizeTier(whiteMatches, powerBallMatch));

        return result;
    }

    private String determinePrizeTier(int whiteMatches, boolean powerBallMatch) {
        if (whiteMatches == 5 && powerBallMatch) return "Jackpot";
        if (whiteMatches == 5) return "Match 5";
        if (whiteMatches == 4 && powerBallMatch) return "Match 4 + PowerBall";
        if (whiteMatches == 4) return "Match 4";
        if (whiteMatches == 3 && powerBallMatch) return "Match 3 + PowerBall";
        if (whiteMatches == 3) return "Match 3";
        if (whiteMatches == 2 && powerBallMatch) return "Match 2 + PowerBall";
        if (whiteMatches == 1 && powerBallMatch) return "Match 1 + PowerBall";
        if (powerBallMatch) return "PowerBall Only";
        return "No Prize";
    }
    private BigDecimal determinePrizeAmount(int whiteMatches, boolean powerBallMatch) {
        if (whiteMatches == 5 && powerBallMatch) return new BigDecimal("100000000");
        if (whiteMatches == 5) return new BigDecimal("1000000");
        if (whiteMatches == 4 && powerBallMatch) return new BigDecimal("50000");
        if (whiteMatches == 4) return new BigDecimal("100");
        if (whiteMatches == 3 && powerBallMatch) return new BigDecimal("100");
        if (whiteMatches == 3) return new BigDecimal("7");
        if (whiteMatches == 2 && powerBallMatch) return new BigDecimal("7");
        if (whiteMatches == 1 && powerBallMatch) return new BigDecimal("4");
        if (powerBallMatch) return new BigDecimal("4");
        return BigDecimal.ZERO;
    }

    public ResultsStatisticsResponse getStatistics(User user) {
        var results = playResultRepository.findByUserOrderByCheckedAtDesc(user);

        var winners = results.stream()
                .filter(result -> !"No Prize".equals(result.getPrizeTier()))
                .toList();

        int jackpots = (int) results.stream()
                .filter(result -> "Jackpot".equals(result.getPrizeTier()))
                .count();

        int bestWhiteMatch = results.stream()
                .mapToInt(PlayResult::getWhiteBallMatches)
                .max()
                .orElse(0);

        int powerBallMatches = (int) results.stream()
                .filter(result -> Boolean.TRUE.equals(result.getPowerBallMatch()))
                .count();

        var totalPrizeValue = winners.stream()
                .map(result -> result.getPrizeAmount() == null ? BigDecimal.ZERO : result.getPrizeAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var recentWinners = winners.stream()
                .limit(25)
                .map(result -> new ResultsStatisticsResponse.WinnerSummary(
                        result.getId(),
                        result.getSavedPlay().getId(),
                        result.getDrawing().getId(),
                        result.getDrawing().getDrawingDate().toString(),
                        result.getWhiteBallMatches(),
                        result.getPowerBallMatch(),
                        result.getPrizeTier(),
                        result.getPrizeAmount()
                ))
                .toList();

        return new ResultsStatisticsResponse(
                results.size(),
                winners.size(),
                jackpots,
                bestWhiteMatch,
                powerBallMatches,
                totalPrizeValue,
                recentWinners
        );
    }

    public int checkDrawingAgainstAllSavedPlays(PowerBallDrawing drawing) {
        var allPlays = savedPlayRepository.findAll();
        int created = 0;

        for (var play : allPlays) {
            var user = play.getUser();

            var existing = playResultRepository
                    .findByUserAndSavedPlayAndDrawing(user, play, drawing);

            if (existing.isPresent()) {
                continue;
            }

            var result = calculateResult(user, play, drawing);
            playResultRepository.save(result);
            created++;
        }

        return created;
    }
}