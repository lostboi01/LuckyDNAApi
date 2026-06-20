package com.thesnellai.luckydna.arenas;

import java.util.List;

public record NumberReason(
        Integer number,
        String numberType,
        Double influenceScore,
        List<String> reasons
) {
}
