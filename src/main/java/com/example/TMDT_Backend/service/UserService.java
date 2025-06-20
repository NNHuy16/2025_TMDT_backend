package com.example.TMDT_Backend.service;

import com.example.TMDT_Backend.dto.request.ChangeEmailRequest;
import com.example.TMDT_Backend.dto.request.LoginRequest;
import com.example.TMDT_Backend.dto.request.RegisterRequest;
import com.example.TMDT_Backend.dto.response.LoginResponse;
import com.example.TMDT_Backend.entity.User;
import com.example.TMDT_Backend.entity.enums.Role;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

public interface UserService {
    LoginResponse login(LoginRequest request);

    User registerUserLocal(RegisterRequest registerRequest);

    User loginReisterUserGoogle(OAuth2AuthenticationToken auth);

    Optional<User> getUserById(Long id);

    Optional<User> getUserByEmail(String email);

    List<User> getAllUsers();

    List<User> getUsersByRole(Role role);

    User updateUserProfile(String email, User newUserData);

    void deleteUser(Long id);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    void changePassword(String email, String oldPassword, String newPassword, String confirmNewPassword);

    void requestEmailChange(User currentUser, ChangeEmailRequest request);

    void confirmEmailChange(String token);

    Optional<User> findByEmail(String email);

    void createPasswordResetToken(String email);

    void resetPassword(String token, String newPassword);
    public User getUserFromPrincipal(Principal principal);
}
