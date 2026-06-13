package com.thesnellai.luckydna.repositories;

import com.thesnellai.luckydna.models.PowerBallDrawing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface PowerBallDrawingRepository extends JpaRepository<PowerBallDrawing, Long> {
    Optional<PowerBallDrawing> findByDrawingDate(LocalDate drawingDate);

    boolean existsByDrawingDate(LocalDate drawingDate);
}
