package com.thesnellai.luckydna.repositories;

import com.thesnellai.luckydna.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByEmail(String email);
}
