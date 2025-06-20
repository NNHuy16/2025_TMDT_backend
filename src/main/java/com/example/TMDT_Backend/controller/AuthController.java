package com.example.TMDT_Backend.controller;

import com.example.TMDT_Backend.dto.request.LoginRequest;
import com.example.TMDT_Backend.dto.request.RegisterRequest;
import com.example.TMDT_Backend.dto.request.ResetPasswordRequest;
import com.example.TMDT_Backend.dto.response.LoginResponse;
import com.example.TMDT_Backend.entity.User;
import com.example.TMDT_Backend.entity.VerificationToken;
import com.example.TMDT_Backend.repository.UserRepository;
import com.example.TMDT_Backend.repository.VerificationTokenRepository;
import com.example.TMDT_Backend.service.UserService;
import com.example.TMDT_Backend.service.VerificationTokenService;
import com.example.TMDT_Backend.service.impl.EmailServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private VerificationTokenService verificationTokenService;

    @Autowired
    private EmailServiceImpl emailService;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private UserRepository userRepository;


    /**
     * Đăng nhập người dùng với email và mật khẩu.
     * @param request Thông tin đăng nhập (email, password).
     * @return Trả về JWT token nếu thành công, hoặc thông báo lỗi nếu thất bại.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.login(request);
            return ResponseEntity.ok(response);
        } catch (DisabledException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Tài khoản chưa được xác thực qua email");
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email hoặc mật khẩu không đúng");
        }
    }


    /**
     * Đăng ký tài khoản mới.
     * @param registerRequest Thông tin đăng ký.
     * @param bindingResult Kết quả validate đầu vào.
     * @param request Đối tượng request của servlet để tạo đường dẫn xác minh.
     * @return Trả về thông báo kết quả đăng ký.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest, BindingResult bindingResult,
                                      HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors()
                    .stream()
                    .map(err -> err.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            User savedUser = userService.registerUserLocal(registerRequest);

            // Tạo verification token
            String token = UUID.randomUUID().toString();
            verificationTokenService.createToken(savedUser, token);

            // Gửi email
            String verifyLink = "http://localhost:8080/verify-email?token=" + token;
            emailService.sendVerificationEmail(savedUser.getEmail(), verifyLink);

            return ResponseEntity.ok("Đăng ký thành công. Vui lòng kiểm tra email để xác minh tài khoản.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Điều hướng sang trang đăng nhập bằng Google OAuth2.
     */
    @GetMapping("/login/google")
    public void loginWithGoogle(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google");
    }


    /**
     * Xác minh tài khoản qua email bằng cách truyền token.
     * @param token Mã xác minh được gửi qua email.
     * @return Trả về thông báo kết quả xác minh.
     */
    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        VerificationToken vt = verificationTokenService.findByToken(token);
        if (vt == null || vt.isExpired()) {
            return ResponseEntity.badRequest().body("Token không hợp lệ hoặc đã hết hạn.");
        }

        User user = vt.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        verificationTokenService.deleteToken(vt);

        return ResponseEntity.ok("Tài khoản đã được xác minh thành công.");
    }


    /**
     * Gửi lại email xác minh tài khoản nếu người dùng chưa xác thực.
     * @param email Email người dùng.
     * @param request Đối tượng request để dựng lại đường dẫn xác minh.
     * @return Trả về thông báo kết quả gửi lại email.
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestParam String email, HttpServletRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Email không tồn tại.");
        }

        User user = userOpt.get();
        if (user.isEnabled()) {
            return ResponseEntity.badRequest().body("Tài khoản đã được xác minh.");
        }

        // Xoá token cũ nếu có
        VerificationToken oldToken = verificationTokenRepository.findByUser(user);
        if (oldToken != null) {
            verificationTokenRepository.delete(oldToken);
        }

        // Tạo token mới
        String newToken = UUID.randomUUID().toString();
        VerificationToken token = new VerificationToken(newToken, user, LocalDateTime.now().plusMinutes(15));
        verificationTokenRepository.save(token);

        // Gửi lại email
        String siteURL = request.getRequestURL().toString().replace(request.getServletPath(), "");
        String verifyLink = siteURL + "/verify?token=" + newToken;
        emailService.sendVerificationEmail(user.getEmail(), verifyLink);

        return ResponseEntity.ok("Email xác minh đã được gửi lại.");
    }


    /**
     * Gửi email khôi phục mật khẩu nếu quên.
     * @param email Email người dùng muốn khôi phục mật khẩu.
     * @return Thông báo kết quả gửi email.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        try {
            userService.createPasswordResetToken(email);
            return ResponseEntity.ok("Đã gửi email đặt lại mật khẩu");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    /**
     * Đặt lại mật khẩu mới bằng token hợp lệ.
     * @param request Chứa token và mật khẩu mới.
     * @return Trả về thông báo kết quả đặt lại mật khẩu.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            userService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok("Đổi mật khẩu thành công");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }




}