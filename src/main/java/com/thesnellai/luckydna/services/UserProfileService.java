package com.thesnellai.luckydna.services;

import com.thesnellai.luckydna.dto.ProfileRequest;
import com.thesnellai.luckydna.dto.ProfileResponse;
import com.thesnellai.luckydna.models.User;
import com.thesnellai.luckydna.models.UserProfile;
import com.thesnellai.luckydna.repositories.UserProfileRepository;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService {
    private final UserProfileRepository repository;

    public UserProfileService(UserProfileRepository repository) {
        this.repository = repository;
    }

    public ProfileResponse getProfile(User user) {
        var profile = repository.findByUser(user).orElseGet(() -> {
            var created = new UserProfile();
            created.setUser(user);
            return repository.save(created);
        });

        return toResponse(profile);
    }

    public ProfileResponse updateProfile(User user, ProfileRequest request) {
        var profile = repository.findByUser(user).orElseGet(() -> {
            var created = new UserProfile();
            created.setUser(user);
            return created;
        });

        profile.setBirthday(request.birthday());
        profile.setFavoriteNumbers(request.favoriteNumbers());
        profile.setLuckyAttributes(request.luckyAttributes());

        return toResponse(repository.save(profile));
    }

    private ProfileResponse toResponse(UserProfile profile) {
        return new ProfileResponse(
                profile.getId(),
                profile.getBirthday(),
                profile.getFavoriteNumbers(),
                profile.getLuckyAttributes()
        );
    }
}
