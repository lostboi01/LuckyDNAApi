package com.thesnellai.luckydna.framework;

import java.util.List;
import java.util.Map;

public record PredictionMatrixResult(
        Map<Integer, Double> primaryNumberScores,
        Map<Integer, Double> bonusNumberScores,
        Map<Integer, List<String>> primaryNumberReasons,
        Map<Integer, List<String>> bonusNumberReasons,
        List<String> methodsUsed,
        double matrixScore
) {
}