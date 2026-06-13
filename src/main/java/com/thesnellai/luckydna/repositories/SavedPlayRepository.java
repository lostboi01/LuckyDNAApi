package com.thesnellai.luckydna.repositories;

import com.thesnellai.luckydna.models.User;
import com.thesnellai.luckydna.models.SavedPlay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavedPlayRepository extends JpaRepository<SavedPlay, Long> {
    List<SavedPlay> findByUserOrderByCreatedAtDesc(User user);
}
