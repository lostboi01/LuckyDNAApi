package com.thesnellai.luckydna.controllers;

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

    public record SavePlayRequest(
            LocalDate playDate,
            @Min(1) @Max(69) Integer whiteBall1,
            @Min(1) @Max(69) Integer whiteBall2,
            @Min(1) @Max(69) Integer whiteBall3,
            @Min(1) @Max(69) Integer whiteBall4,
            @Min(1) @Max(69) Integer whiteBall5,
            @Min(1) @Max(26) Integer powerBall
    ) {}

    public record SavedPlayResponse(
            Long id,
            Integer whiteBall1,
            Integer whiteBall2,
            Integer whiteBall3,
            Integer whiteBall4,
            Integer whiteBall5,
            Integer powerBall,
            Instant createdAt
    ) {
        static SavedPlayResponse from(SavedPlay play) {
            return new SavedPlayResponse(
                    play.getId(),
                    play.getWhiteBall1(),
                    play.getWhiteBall2(),
                    play.getWhiteBall3(),
                    play.getWhiteBall4(),
                    play.getWhiteBall5(),
                    play.getPowerBall(),
                    play.getCreatedAt()
            );
        }
    }
}
