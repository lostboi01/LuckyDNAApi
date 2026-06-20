package com.thesnellai.luckydna.arenas.lottery;

import com.thesnellai.luckydna.arenas.ArenaRules;

import java.math.BigDecimal;

public record LotteryArenaRules(
        int primaryCount,
        int primaryMin,
        int primaryMax,
        int bonusCount,
        int bonusMin,
        int bonusMax,
        boolean allowDuplicatePrimary,
        int minBoards,
        int maxBoards,
        int minAdvanceDraws,
        int maxAdvanceDraws,
        BigDecimal baseCostPerBoard,
        boolean supportsPowerPlay,
        BigDecimal powerPlayCostPerBoard,
        boolean supportsDoublePlay,
        BigDecimal doublePlayCostPerBoard
) implements ArenaRules {
}