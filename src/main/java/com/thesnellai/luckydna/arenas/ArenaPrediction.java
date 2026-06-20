package com.thesnellai.luckydna.arenas;

import java.util.List;

public record ArenaPrediction(
        ArenaCode arenaCode,
        String arenaName,
        List<Integer> primaryNumbers,
        List<Integer> bonusNumbers,
        double predictionScore,
        List<String> methodsUsed,
        List<NumberReason> numberReasons
) {
}