package com.thesnellai.luckydna.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "saved_play")
public class SavedPlay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer whiteBall1;
    private Integer whiteBall2;
    private Integer whiteBall3;
    private Integer whiteBall4;
    private Integer whiteBall5;
    private Integer powerBall;

    private Instant createdAt = Instant.now();
    private LocalDate playDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;
}
