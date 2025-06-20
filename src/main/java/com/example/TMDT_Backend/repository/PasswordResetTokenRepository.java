package com.example.TMDT_Backend.repository;

import com.example.TMDT_Backend.entity.PasswordResetToken;
import com.example.TMDT_Backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {
    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUser(User user);
}
