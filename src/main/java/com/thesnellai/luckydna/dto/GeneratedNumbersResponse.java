package com.thesnellai.luckydna.dto;

import java.util.List;

public record GeneratedNumbersResponse(
        List<Integer> whiteBalls,
        Integer powerBall
) {}
