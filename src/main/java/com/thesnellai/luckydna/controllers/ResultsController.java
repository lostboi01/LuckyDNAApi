package com.thesnellai.luckydna.controllers;

import com.thesnellai.luckydna.dto.CheckAllResultsResponse;
import com.thesnellai.luckydna.dto.ResultCheckRequest;
import com.thesnellai.luckydna.dto.ResultCheckResponse;
import com.thesnellai.luckydna.dto.ResultsStatisticsResponse;
import com.thesnellai.luckydna.models.User;
import com.thesnellai.luckydna.services.PlayResultService;
import com.thesnellai.luckydna.services.ResultsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/results")
public class ResultsController {
    private final ResultsService service;
    private final PlayResultService playResultService;

    public ResultsController(
            ResultsService service,
            PlayResultService playResultService
    ) {
        this.service = service;
        this.playResultService = playResultService;
    }

    @PostMapping("/check")
    public ResultCheckResponse check(@RequestBody ResultCheckRequest request) {
        return service.check(request);
    }

    @PostMapping("/check-all")
    public CheckAllResultsResponse checkAll(@AuthenticationPrincipal User user) {
        return playResultService.checkAll(user);
    }

    @GetMapping("/statistics")
    public ResultsStatisticsResponse statistics(@AuthenticationPrincipal User user) {
        return playResultService.getStatistics(user);
    }
}