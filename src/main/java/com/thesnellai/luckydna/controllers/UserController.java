package com.thesnellai.luckydna.controllers;

import com.thesnellai.luckydna.models.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me")
public class UserController {
    @GetMapping
    public UserResponse me(@AuthenticationPrincipal User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail());
    }

    public record UserResponse(Long id, String fullName, String email) {}
}
