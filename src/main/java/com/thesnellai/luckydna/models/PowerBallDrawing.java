package com.thesnellai.luckydna.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "powerball_drawing")
public class PowerBallDrawing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate drawingDate;

    private Integer whiteBall1;
    private Integer whiteBall2;
    private Integer whiteBall3;
    private Integer whiteBall4;
    private Integer whiteBall5;
    private Integer powerBall;
}
