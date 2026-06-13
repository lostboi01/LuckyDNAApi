package com.thesnellai.luckydna.services;

import com.thesnellai.luckydna.dto.ResultCheckRequest;
import com.thesnellai.luckydna.dto.ResultCheckResponse;
import com.thesnellai.luckydna.models.PowerBallDrawing;
import com.thesnellai.luckydna.repositories.PowerBallDrawingRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class ResultsService {

    private final PowerBallDrawingRepository drawingRepository;

    public ResultsService(PowerBallDrawingRepository drawingRepository) {
        this.drawingRepository = drawingRepository;
    }

    public ResultCheckResponse check(ResultCheckRequest request) {

        PowerBallDrawing drawing = drawingRepository
                .findByDrawingDate(request.drawingDate())
                .orElseThrow(() ->
                        new IllegalArgumentException("Drawing not found."));

        Set<Integer> drawingBalls = Set.of(
                drawing.getWhiteBall1(),
                drawing.getWhiteBall2(),
                drawing.getWhiteBall3(),
                drawing.getWhiteBall4(),
                drawing.getWhiteBall5()
        );

        Set<Integer> playerBalls = new HashSet<>();

        playerBalls.add(request.whiteBall1());
        playerBalls.add(request.whiteBall2());
        playerBalls.add(request.whiteBall3());
        playerBalls.add(request.whiteBall4());
        playerBalls.add(request.whiteBall5());

        playerBalls.retainAll(drawingBalls);

        int whiteBallMatches = playerBalls.size();

        boolean powerBallMatch =
                drawing.getPowerBall().equals(request.powerBall());

        return new ResultCheckResponse(
                whiteBallMatches,
                powerBallMatch,
                determinePrizeTier(
                        whiteBallMatches,
                        powerBallMatch
                )
        );
    }

    private String determinePrizeTier(
            int whiteMatches,
            boolean powerBallMatch
    ) {

        if (whiteMatches == 5 && powerBallMatch)
            return "Jackpot";

        if (whiteMatches == 5)
            return "Match 5";

        if (whiteMatches == 4 && powerBallMatch)
            return "Match 4 + PowerBall";

        if (whiteMatches == 4)
            return "Match 4";

        if (whiteMatches == 3 && powerBallMatch)
            return "Match 3 + PowerBall";

        if (whiteMatches == 3)
            return "Match 3";

        if (whiteMatches == 2 && powerBallMatch)
            return "Match 2 + PowerBall";

        if (whiteMatches == 1 && powerBallMatch)
            return "Match 1 + PowerBall";

        if (powerBallMatch)
            return "PowerBall Only";

        return "No Prize";
    }
}