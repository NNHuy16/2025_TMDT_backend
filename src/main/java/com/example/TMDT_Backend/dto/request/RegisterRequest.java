package com.example.TMDT_Backend.dto.request;

import com.example.TMDT_Backend.dto.PasswordConfirmable;
import com.example.TMDT_Backend.validator.annotation.PasswordMatches;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@PasswordMatches(message = "Mật khẩu xác nhận không khớp")
public class RegisterRequest implements PasswordConfirmable {

    @NotBlank(message = "Tên không được để trống")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "Tên chỉ được chứa chữ cái và khoảng trắng")
    private String firstName;

    @NotBlank(message = "Họ không được để trống")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "Họ chỉ được chứa chữ cái và khoảng trắng")
    private String lastName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự")
    private String password;

    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự")
    private String confirmPassword;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;

    @NotNull(message = "Ngày sinh không được để trống")
    @Past(message = "Ngày sinh phải nằm trong quá khứ")
    private LocalDate dateOfBirth;

    @Pattern(regexp = "^(http|https)://.*$", message = "Avatar phải là URL hợp lệ", flags = Pattern.Flag.CASE_INSENSITIVE)
    private String logoUrl;

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getConfirmPassword() {
        return confirmPassword;
    }
}
