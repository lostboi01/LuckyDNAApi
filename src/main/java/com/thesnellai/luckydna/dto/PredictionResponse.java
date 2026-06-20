package com.thesnellai.luckydna.dto;

import com.thesnellai.luckydna.arenas.ArenaPrediction;

import java.util.List;

public record PredictionResponse(
        List<ArenaPrediction> arenaPredictions,
        Double predictionMatrixScore,
        Boolean autoSaved,
        List<String> explanations
) {
}