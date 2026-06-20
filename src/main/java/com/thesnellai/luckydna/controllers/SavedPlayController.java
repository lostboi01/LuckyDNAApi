package com.thesnellai.luckydna.controllers;

import com.thesnellai.luckydna.arenas.ArenaCode;
import com.thesnellai.luckydna.dto.SavePlayRequest;
import com.thesnellai.luckydna.models.User;
import com.thesnellai.luckydna.models.SavedPlay;
import com.thesnellai.luckydna.repositories.SavedPlayRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/plays")
public class SavedPlayController {
    private final SavedPlayRepository repository;

    public SavedPlayController(SavedPlayRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<SavedPlayResponse> getPlays(@AuthenticationPrincipal User user) {
        return repository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(SavedPlayResponse::from)
                .toList();
    }

    @PostMapping
    public SavedPlayResponse savePlay(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody SavePlayRequest request
    ) {
        var play = new SavedPlay();
        play.setUser(user);

        System.out.println("Save request arenaCode = " + request.arenaCode());

        play.setArenaCode(
                request.arenaCode() == null
                        ? ArenaCode.POWERBALL
                        : request.arenaCode()
        );

        play.setPlayDate(request.playDate());
        play.setWhiteBall1(request.whiteBall1());
        play.setWhiteBall2(request.whiteBall2());
        play.setWhiteBall3(request.whiteBall3());
        play.setWhiteBall4(request.whiteBall4());
        play.setWhiteBall5(request.whiteBall5());
        play.setPowerBall(request.powerBall());

        return SavedPlayResponse.from(repository.save(play));
    }

    @DeleteMapping("/{id}")
    public void deletePlay(@AuthenticationPrincipal User user, @PathVariable Long id) {
        var play = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Play not found."));

        if (!play.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Play not found.");
        }

        repository.delete(play);
    }



    public record SavedPlayResponse(
            Long id,
            ArenaCode arenaCode,
            Integer whiteBall1,
            Integer whiteBall2,
            Integer whiteBall3,
            Integer whiteBall4,
            Integer whiteBall5,
            Integer powerBall,
            LocalDate playDate,
            Instant createdAt
    ) {
        static SavedPlayResponse from(SavedPlay play) {
            return new SavedPlayResponse(
                    play.getId(),
                    play.getArenaCode(),
                    play.getWhiteBall1(),
                    play.getWhiteBall2(),
                    play.getWhiteBall3(),
                    play.getWhiteBall4(),
                    play.getWhiteBall5(),
                    play.getPowerBall(),
                    play.getPlayDate(),
                    play.getCreatedAt()
            );
        }
    }
}
