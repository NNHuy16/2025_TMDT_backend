package com.example.TMDT_Backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class ChangeEmailRequest {
    @NotBlank(message = "Email mới không được để trống")
    @Email(message = "Email mới không hợp lệ")
    private String newEmail;

    @NotBlank(message = "Mật khẩu không được để trống") // nếu bạn yêu cầu xác minh
    private String password;
}
