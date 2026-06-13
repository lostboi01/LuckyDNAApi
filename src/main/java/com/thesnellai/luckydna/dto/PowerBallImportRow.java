package com.thesnellai.luckydna.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PowerBallImportRow(
        @JsonProperty("draw_date") String drawDate,
        @JsonProperty("winning_numbers") String winningNumbers,
        @JsonProperty("multiplier") String multiplier
) {}