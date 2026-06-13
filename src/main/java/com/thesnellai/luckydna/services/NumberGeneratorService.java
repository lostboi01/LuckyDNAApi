package com.thesnellai.luckydna.services;

import com.thesnellai.luckydna.dto.GeneratedNumbersResponse;
import com.thesnellai.luckydna.models.User;
import com.thesnellai.luckydna.repositories.UserProfileRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class NumberGeneratorService {
    private final UserProfileRepository profileRepository;

    public NumberGeneratorService(UserProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public GeneratedNumbersResponse generate(User user) {
        var profile = profileRepository.findByUser(user).orElse(null);
        var preferred = new ArrayList<Integer>();

        if (profile != null && profile.getFavoriteNumbers() != null) {
            preferred.addAll(Arrays.stream(profile.getFavoriteNumbers().split(","))
                    .map(String::trim)
                    .filter(s -> s.matches("\\d+"))
                    .map(Integer::parseInt)
                    .filter(n -> n >= 1 && n <= 69)
                    .distinct()
                    .toList());
        }

        var random = new Random();
        var whiteBalls = new LinkedHashSet<Integer>();

        Collections.shuffle(preferred);

        for (var number : preferred) {
            if (whiteBalls.size() < 5) {
                whiteBalls.add(number);
            }
        }

        var remaining = IntStream.rangeClosed(1, 69)
                .boxed()
                .filter(n -> !whiteBalls.contains(n))
                .collect(Collectors.toCollection(ArrayList::new));

        Collections.shuffle(remaining);

        for (var number : remaining) {
            if (whiteBalls.size() < 5) {
                whiteBalls.add(number);
            }
        }

        var sorted = whiteBalls.stream().sorted().toList();
        var powerBall = random.nextInt(26) + 1;

        return new GeneratedNumbersResponse(sorted, powerBall);
    }
}
