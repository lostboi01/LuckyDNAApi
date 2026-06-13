package com.thesnellai.luckydna.controllers;

import com.thesnellai.luckydna.dto.GeneratedNumbersResponse;
import com.thesnellai.luckydna.models.User;
import com.thesnellai.luckydna.services.NumberGeneratorService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/generator")
public class GeneratorController {
    private final NumberGeneratorService service;

    public GeneratorController(NumberGeneratorService service) {
        this.service = service;
    }

    @PostMapping
    public GeneratedNumbersResponse generate(@AuthenticationPrincipal User user) {
        return service.generate(user);
    }
}
