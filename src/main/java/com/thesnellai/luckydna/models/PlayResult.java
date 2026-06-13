package com.thesnellai.luckydna.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "play_result")
public class PlayResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer whiteBallMatches;
    private Boolean powerBallMatch;
    private String prizeTier;
    private Instant checkedAt = Instant.now();
    private BigDecimal prizeAmount;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "saved_play_id")
    private SavedPlay savedPlay;

    @ManyToOne(optional = false)
    @JoinColumn(name = "drawing_id")
    private PowerBallDrawing drawing;
}