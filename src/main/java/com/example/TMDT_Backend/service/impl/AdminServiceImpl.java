package com.example.TMDT_Backend.service.impl;

import com.example.TMDT_Backend.entity.User;
import com.example.TMDT_Backend.entity.enums.Role;
import com.example.TMDT_Backend.repository.UserRepository;
import com.example.TMDT_Backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User createUsers(User user) {
        if (user.getRole() == null) {
            user.setRole(Role.USER);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public Optional<User> getAdminById(Long id) {
        return userRepository.findById(id)
                .filter(user -> user.getRole() == Role.ADMIN);
    }

    @Override
    public Optional<User> getAdminByEmail(String email) {
        return userRepository.findByEmail(email)
                .filter(user -> user.getRole() == Role.ADMIN);
    }

    @Override
    public List<User> getAllAdmins() {
        return userRepository.findByRole(Role.ADMIN);
    }

    @Override
    public User updateAdmin(User user) {
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        user.setRole(Role.ADMIN);
        return userRepository.save(user);
    }

    @Override
    public void deleteAdmin(Long id) {
        userRepository.deleteById(id);
    }

}

