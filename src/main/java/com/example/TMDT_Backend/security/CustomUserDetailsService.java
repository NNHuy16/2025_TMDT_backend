package com.example.TMDT_Backend.security;

import com.example.TMDT_Backend.entity.User;
import com.example.TMDT_Backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with email: " + email));

        // Lấy role của user (ví dụ USER, EMPLOYER, ADMIN)
        String role = "ROLE_" + user.getRole().name();

        return new CustomUserDetails(user);
    }

}
