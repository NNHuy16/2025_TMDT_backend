package com.example.TMDT_Backend.service.impl;

import com.example.TMDT_Backend.dto.request.LoginRequest;
import com.example.TMDT_Backend.dto.response.LoginResponse;
import com.example.TMDT_Backend.dto.request.ChangeEmailRequest;
import com.example.TMDT_Backend.dto.request.RegisterRequest;
import com.example.TMDT_Backend.entity.*;
import com.example.TMDT_Backend.entity.enums.*;
import com.example.TMDT_Backend.mapper.UserMapper;
import com.example.TMDT_Backend.repository.*;
import com.example.TMDT_Backend.security.JwtTokenProvider;
import com.example.TMDT_Backend.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

@AllArgsConstructor
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtTokenProvider tokenProvider;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailServiceImpl emailService;
    private final EmailChangeTokenRepository emailChangeTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;


    public LoginResponse login(LoginRequest request) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        String token = tokenProvider.generateToken(auth);
        return new LoginResponse(token, auth.getAuthorities().toString());
    }

    @Transactional
    public User registerUserLocal(RegisterRequest registerRequest) {
        Optional<User> existingUserOpt = userRepository.findByEmail(registerRequest.getEmail());

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            if (!existingUser.isEnabled()) {
                // Kiểm tra token
                VerificationToken oldToken = verificationTokenRepository.findByUser(existingUser);
                if (oldToken != null && oldToken.isExpired()) {
                    // Xoá token và user cũ
                    verificationTokenRepository.delete(oldToken);
                    userRepository.delete(existingUser);
                } else {
                    throw new IllegalArgumentException("Email đã được đăng ký nhưng chưa xác minh. Vui lòng kiểm tra email hoặc yêu cầu gửi lại email xác minh.");
                }
            } else {
                throw new IllegalArgumentException("Email đã được sử dụng.");
            }

        }
        if (userRepository.existsByPhoneNumber(registerRequest.getPhoneNumber())) {
            throw new IllegalArgumentException("Số điện thoại đã được sử dụng.");
        }

        User user = userMapper.toEntity(registerRequest);
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        return userRepository.save(user);
    }



    @Transactional
    public User loginReisterUserGoogle(OAuth2AuthenticationToken auth) {
        OAuth2User oAuth2User = auth.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User existingUser = optionalUser.get();
            if (existingUser.getProvider() != AuthProvider.GOOGLE) {
                // Người dùng đã đăng ký local, không cho login Google
                throw new IllegalArgumentException("Tài khoản đã tồn tại.");
            }

            return existingUser;
        }

        User user = new User();
        user.setEmail(email);
        user.setFullName(name);
        user.setLogoUrl(picture);
        user.setRole(Role.USER);
        user.setProvider(AuthProvider.GOOGLE);

        return userRepository.save(user);
    }

//    public User loginReisterUserFacebook(OAuth2AuthenticationToken auth) {
//        OAuth2User oAuth2User = auth.getPrincipal();
//        // Lấy thông tin user từ Facebook
//        String email = oAuth2User.getAttribute("email");
//        String name = oAuth2User.getAttribute("name");
//        String picture = oAuth2User.getAttribute("picture"); // Có thể khác tùy scope Facebook trả về
//
//        // Kiểm tra user đã tồn tại chưa
//        Optional<User> optionalUser = userRepository.findByEmail(email);
//        if (optionalUser.isPresent()) {
//            return optionalUser.get();
//        }
//
//        User user = new User();
//        user.setEmail(email);
//        user.setFullName(name);
//        user.setLogoUrl(picture);
//        user.setRole(Role.USER);
//        user.setProvider(AuthProvider.FACEBOOK);
//
//        return userRepository.save(user);
//    }


    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    public User updateUserProfile(String email, User newUserData) {
        User existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        existingUser.setFullName(newUserData.getFullName());
        existingUser.setPhoneNumber(newUserData.getPhoneNumber());
        existingUser.setDateOfBirth(newUserData.getDateOfBirth());
        existingUser.setLogoUrl(newUserData.getLogoUrl());

        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByPhoneNumber(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }


    public void changePassword(String email, String oldPassword, String newPassword, String confirmNewPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không đúng");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void requestEmailChange(User currentUser, ChangeEmailRequest request) {
        if (!passwordEncoder.matches(request.getPassword(), currentUser.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu không đúng");
        }

        if (userRepository.existsByEmail(request.getNewEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        if (emailChangeTokenRepository.existsByUserAndExpiryDateAfter(currentUser, LocalDateTime.now())) {
            throw new IllegalArgumentException("Bạn đã gửi yêu cầu đổi email gần đây, vui lòng kiểm tra email để xác nhận.");
        }
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        boolean hasRecentRequest = emailChangeTokenRepository.existsByUserAndCreatedDateAfter(currentUser, twentyFourHoursAgo);

        if (hasRecentRequest) {
            throw new IllegalArgumentException("Bạn chỉ được đổi email 1 lần trong 24 giờ.");
        }


        // Xóa các token cũ nếu cần
        emailChangeTokenRepository.deleteAllByUser(currentUser);

        String token = UUID.randomUUID().toString();
        EmailChangeToken emailChangeToken = new EmailChangeToken(
                token,
                request.getNewEmail(),
                LocalDateTime.now().plusMinutes(15),
                currentUser
        );

        emailChangeTokenRepository.save(emailChangeToken);

        // Gửi email xác nhận
        String link = "http://localhost:3000/confirm-email-change?token=" + token;
        emailService.sendEmail(
                request.getNewEmail(),
                "Xác nhận thay đổi email",
                "Vui lòng nhấn vào liên kết để xác nhận thay đổi email: " + link
        );
    }


    @Transactional
    public void confirmEmailChange(String token) {
        EmailChangeToken emailChangeToken = emailChangeTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token không hợp lệ"));

        if (emailChangeToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token đã hết hạn");
        }

        User user = emailChangeToken.getUser();
        user.setEmail(emailChangeToken.getNewEmail());

        userRepository.save(user);
        emailChangeTokenRepository.delete(emailChangeToken);
    }

    @Transactional
    public void createPasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email không tồn tại"));

        // Xóa token cũ nếu có
        passwordResetTokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(
                token,
                LocalDateTime.now().plusMinutes(15),
                user
        );

        passwordResetTokenRepository.save(resetToken);

        String link = "http://localhost:3000/reset-password?token=" + token;
        emailService.sendEmail(email, "Đặt lại mật khẩu", "Nhấn vào link sau để đặt lại mật khẩu: " + link);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token không hợp lệ"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token đã hết hạn");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
    }
    public User getUserFromPrincipal(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));
    }


}
