package com.example.TMDT_Backend.dto.response;

import com.example.TMDT_Backend.entity.User;
import com.example.TMDT_Backend.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String logoUrl;
    private Role role;
    private boolean enabled;

    public UserDTO(User user) {
        this.id = user.getId();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();
        this.dateOfBirth = user.getDateOfBirth();
        this.logoUrl = user.getLogoUrl();
        this.role = user.getRole();
        this.enabled = user.isEnabled();
    }

}
