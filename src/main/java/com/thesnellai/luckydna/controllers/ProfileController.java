package com.thesnellai.luckydna.controllers;

import com.thesnellai.luckydna.dto.ProfileRequest;
import com.thesnellai.luckydna.dto.ProfileResponse;
import com.thesnellai.luckydna.models.User;
import com.thesnellai.luckydna.services.UserProfileService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final UserProfileService service;

    public ProfileController(UserProfileService service) {
        this.service = service;
    }

    @GetMapping
    public ProfileResponse getProfile(@AuthenticationPrincipal User user) {
        return service.getProfile(user);
    }

    @PutMapping
    public ProfileResponse updateProfile(
            @AuthenticationPrincipal User user,
            @RequestBody ProfileRequest request
    ) {
        return service.updateProfile(user, request);
    }
}
