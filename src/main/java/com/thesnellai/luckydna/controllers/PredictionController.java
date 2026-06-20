package com.thesnellai.luckydna.controllers;

import com.thesnellai.luckydna.dto.PredictionRequest;
import com.thesnellai.luckydna.dto.PredictionResponse;
import com.thesnellai.luckydna.services.PredictionMatrixService;
import com.thesnellai.luckydna.models.PredictionModel;
import com.thesnellai.luckydna.models.User;
import com.thesnellai.luckydna.services.NumberGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Set;

@Tag(
        name = "Predictions",
        description = "LuckyDNA Predictive Identity Framework and Prediction Matrix endpoints"
)
@RestController
@RequestMapping("/api/predictions")
public class PredictionController {
    private final NumberGeneratorService service;

    private final PredictionMatrixService predictionMatrixService;


    public PredictionController(NumberGeneratorService service,
                                PredictionMatrixService predictionMatrixService) {
        this.service = service;
        this.predictionMatrixService = predictionMatrixService;
    }

//    @PostMapping
//    public GeneratedNumbersResponse generate(@AuthenticationPrincipal User user) {
//        return service.generate(user);
//    }

    @Operation(
            summary = "Run Prediction Matrix",
            description = "Generates a personalized prediction using LuckyDNA Strands and prediction modes."
    )
    @PostMapping
    public ResponseEntity<PredictionResponse> runPrediction(
            Authentication authentication,
            @RequestBody(required = false) PredictionRequest request
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated.");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof User user)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid authenticated user.");
        }

        LocalDate drawDate = request == null || request.drawDate() == null
                ? LocalDate.now()
                : request.drawDate();

        Set<PredictionModel> enabledModels =
                request == null || request.enabledModels() == null
                        ? Set.of()
                        : request.enabledModels();

        PredictionResponse response = predictionMatrixService.runPrediction(
                user.getId(),
                user.getEmail(),
                request
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/arenas")
    public ResponseEntity<?> availableArenas() {
        return ResponseEntity.ok(predictionMatrixService.availableArenas());
    }

    @GetMapping("/test")
    public String test(Authentication authentication) {
        return authentication == null
                ? "NO AUTH"
                : authentication.getName();
    }

}
