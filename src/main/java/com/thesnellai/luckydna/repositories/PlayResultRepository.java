package com.thesnellai.luckydna.repositories;

import com.thesnellai.luckydna.models.User;
import com.thesnellai.luckydna.models.PlayResult;
import com.thesnellai.luckydna.models.PowerBallDrawing;
import com.thesnellai.luckydna.models.SavedPlay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayResultRepository extends JpaRepository<PlayResult, Long> {
    List<PlayResult> findByUserOrderByCheckedAtDesc(User user);

    List<PlayResult> findByUserAndPrizeTierNotOrderByCheckedAtDesc(
            User user,
            String prizeTier
    );

    Optional<PlayResult> findByUserAndSavedPlayAndDrawing(
            User user,
            SavedPlay savedPlay,
            PowerBallDrawing drawing
    );
}