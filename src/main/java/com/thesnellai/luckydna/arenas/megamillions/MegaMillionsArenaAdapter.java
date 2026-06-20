package com.thesnellai.luckydna.arenas.megamillions;

import com.thesnellai.luckydna.arenas.*;
import com.thesnellai.luckydna.arenas.lottery.LotteryArenaRules;
import com.thesnellai.luckydna.framework.PredictionMatrixResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class MegaMillionsArenaAdapter implements ArenaAdapter {

    @Override
    public ArenaCode arenaCode() {
        return ArenaCode.MEGA_MILLIONS;
    }

    @Override
    public ArenaSpecification<LotteryArenaRules> specification() {
        return new ArenaSpecification<>(
                ArenaCode.MEGA_MILLIONS,
                "Mega Millions Arena™",
                ArenaCategory.LOTTERY,
                new LotteryArenaRules(
                        5, 1, 70,
                        1, 1, 25,
                        false,
                        1, 5,
                        1, 15,
                        BigDecimal.valueOf(2.00),
                        false,
                        BigDecimal.ZERO,
                        false,
                        BigDecimal.ZERO
                )
        );
    }

    @Override
    public ArenaPrediction apply(PredictionMatrixResult matrixResult) {
        var spec = specification();
        var rules = spec.rules();

        List<Integer> primaryNumbers = matrixResult.primaryNumberScores()
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey() >= rules.primaryMin())
                .filter(entry -> entry.getKey() <= rules.primaryMax())
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(rules.primaryCount())
                .map(Map.Entry::getKey)
                .sorted()
                .toList();

        List<Integer> bonusNumbers = matrixResult.bonusNumberScores()
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey() >= rules.bonusMin())
                .filter(entry -> entry.getKey() <= rules.bonusMax())
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(rules.bonusCount())
                .map(Map.Entry::getKey)
                .toList();

        List<NumberReason> numberReasons = buildNumberReasons(
                primaryNumbers,
                bonusNumbers,
                matrixResult);

        return new ArenaPrediction(
                arenaCode(),
                spec.displayName(),
                primaryNumbers,
                bonusNumbers,
                matrixResult.matrixScore(),
                matrixResult.methodsUsed(),
                numberReasons
        );
    }

    private List<NumberReason> buildNumberReasons(
            List<Integer> primaryNumbers,
            List<Integer> bonusNumbers,
            PredictionMatrixResult matrixResult
    ) {
        List<NumberReason> reasons = new java.util.ArrayList<>();

        for (Integer number : primaryNumbers) {
            reasons.add(new NumberReason(
                    number,
                    "PRIMARY",
                    matrixResult.primaryNumberScores().getOrDefault(number, 0.0),
                    matrixResult.primaryNumberReasons().getOrDefault(number, List.of())
            ));
        }

        for (Integer number : bonusNumbers) {
            reasons.add(new NumberReason(
                    number,
                    "BONUS",
                    matrixResult.bonusNumberScores().getOrDefault(number, 0.0),
                    matrixResult.bonusNumberReasons().getOrDefault(number, List.of())
            ));
        }

        return reasons;
    }
}