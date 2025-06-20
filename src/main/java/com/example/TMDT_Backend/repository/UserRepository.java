package com.example.TMDT_Backend.repository;

import com.example.TMDT_Backend.entity.User;
import com.example.TMDT_Backend.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
}
