package com.example.TMDT_Backend.controller;

import com.example.TMDT_Backend.dto.request.ChangePasswordRequest;
import com.example.TMDT_Backend.dto.request.ChangeEmailRequest;
import com.example.TMDT_Backend.dto.response.UserDTO;
import com.example.TMDT_Backend.entity.User;
import com.example.TMDT_Backend.helper.SecurityUtil;
import com.example.TMDT_Backend.repository.UserRepository;
import com.example.TMDT_Backend.service.BlacklistService;
import com.example.TMDT_Backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private BlacklistService blacklistService;

    @Autowired
    private UserRepository userRepository;



    /**
     * API cập nhật thông tin cá nhân của người dùng hiện tại.
     * Yêu cầu người dùng đã đăng nhập.
     *
     * @param updatedUser Thông tin cập nhật.
     * @param authentication Thông tin xác thực để lấy email.
     * @return Người dùng sau khi cập nhật.
     */
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody User updatedUser, Authentication authentication) {
        String email = SecurityUtil.extractEmail(authentication);
        User updated = userService.updateUserProfile(email, updatedUser);
        return ResponseEntity.ok(updated);
    }

    /**
     * API đăng xuất người dùng bằng cách đưa JWT vào danh sách blacklist.
     * Yêu cầu người dùng đã đăng nhập.
     *
     * @param authHeader Header chứa token Bearer.
     * @return Thông báo đăng xuất thành công.
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/api/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // bỏ "Bearer "
        blacklistService.addToken(token);  // lưu token vào blacklist
        return ResponseEntity.ok("Logged out");
    }

    /**
     * API thay đổi mật khẩu. Yêu cầu người dùng cung cấp mật khẩu cũ và mật khẩu mới.
     *
     * @param userDetails Người dùng hiện tại (được inject từ Spring Security).
     * @param request Request chứa mật khẩu cũ và mới.
     * @return Thông báo thành công.
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid ChangePasswordRequest request) {

        userService.changePassword(userDetails.getUsername(), request.getOldPassword(), request.getNewPassword(), request.getConfirmNewPassword());
        return ResponseEntity.ok("Đổi mật khẩu thành công");
    }


    /**
     * API yêu cầu thay đổi email. Gửi mã xác nhận tới email mới.
     *
     * @param userDetails Người dùng hiện tại.
     * @param request Dữ liệu chứa email mới.
     * @return Thông báo đã gửi email xác nhận.
     */
    @PostMapping("/change-email")
    public ResponseEntity<String> requestEmailChange(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid ChangeEmailRequest request) {
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        userService.requestEmailChange(user, request);
        return ResponseEntity.ok("Đã gửi email xác nhận tới địa chỉ mới");
    }


    /**
     * API xác nhận thay đổi email bằng token gửi qua email.
     *
     * @param token Mã xác nhận.
     * @return Thông báo kết quả xác nhận.
     */
    @GetMapping("/confirm-email-change")
    public ResponseEntity<String> confirmEmailChange(@RequestParam String token) {
        try {
            userService.confirmEmailChange(token);
            return ResponseEntity.ok("Email đã được thay đổi thành công");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage()); // trả 400 với message
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Đã xảy ra lỗi trong quá trình xử lý.");
        }

    }

    /**
     * API lấy role hiện tại của người dùng đang đăng nhập.
     *
     * @param auth Thông tin xác thực.
     * @return Tên role của người dùng (USER, EMPLOYER, ADMIN).
     */
    @GetMapping("/me")
    public ResponseEntity<String> getCurrentUserRole(Authentication auth) {
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .map(user -> ResponseEntity.ok(user.getRole().name()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .map(user -> ResponseEntity.ok(new UserDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }



}




