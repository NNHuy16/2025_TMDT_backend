package com.example.TMDT_Backend.service;

import com.example.TMDT_Backend.entity.User;
import com.example.TMDT_Backend.entity.VerificationToken;

public interface VerificationTokenService {
    void createToken(User user, String token);
    VerificationToken findByToken(String token);
    void deleteToken(VerificationToken token);
}
