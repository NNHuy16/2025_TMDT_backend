package com.example.TMDT_Backend.service.impl;

import com.example.TMDT_Backend.entity.User;
import com.example.TMDT_Backend.entity.VerificationToken;
import com.example.TMDT_Backend.repository.VerificationTokenRepository;
import com.example.TMDT_Backend.service.VerificationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class VerificationTokenServiceImpl implements VerificationTokenService {

    @Autowired
    private VerificationTokenRepository tokenRepo;

    @Override
    public void createToken(User user, String token) {
        VerificationToken vt = new VerificationToken();
        vt.setToken(token);
        vt.setUser(user);
        vt.setExpiryDate(LocalDateTime.now().plusMinutes(15));
        tokenRepo.save(vt);
    }

    @Override
    public VerificationToken findByToken(String token) {
        return tokenRepo.findByToken(token);
    }

    @Override
    public void deleteToken(VerificationToken token) {
        tokenRepo.delete(token);
    }
}

