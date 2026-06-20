package com.thesnellai.luckydna.arenas;

import com.thesnellai.luckydna.framework.PredictionMatrixResult;

public interface ArenaAdapter {

    ArenaCode arenaCode();

    ArenaSpecification<? extends ArenaRules> specification();

    ArenaPrediction apply(PredictionMatrixResult matrixResult);
}