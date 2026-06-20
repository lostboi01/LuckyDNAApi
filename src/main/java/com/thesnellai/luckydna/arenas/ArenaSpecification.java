package com.thesnellai.luckydna.arenas;

public record ArenaSpecification<T extends ArenaRules>(
        ArenaCode arenaCode,
        String displayName,
        ArenaCategory category,
        T rules
) {
}