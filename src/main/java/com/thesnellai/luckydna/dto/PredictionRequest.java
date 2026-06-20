package com.thesnellai.luckydna.dto;

import com.thesnellai.luckydna.arenas.ArenaCode;
import com.thesnellai.luckydna.models.PredictionModel;
import com.thesnellai.luckydna.models.StrandType;

import java.time.LocalDate;
import java.util.Set;

public record PredictionRequest(
        ArenaCode arenaCode,
        LocalDate drawDate,
        Set<PredictionModel> enabledModels,
        Integer gameCount,
        Boolean autoSavePredictions,
        String predictionRunId
) {
}