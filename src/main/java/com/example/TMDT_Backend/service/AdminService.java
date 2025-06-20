package com.example.TMDT_Backend.service;

import com.example.TMDT_Backend.entity.User;

import java.util.List;
import java.util.Optional;

public interface AdminService {
    User createUsers(User user);

    Optional<User> getAdminById(Long id);

    Optional<User> getAdminByEmail(String email);

    List<User> getAllAdmins();

    User updateAdmin(User user);

    void deleteAdmin(Long id);

}
