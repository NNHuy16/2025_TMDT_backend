package com.example.TMDT_Backend.validator;

import com.example.TMDT_Backend.dto.request.ChangePasswordRequest;
import com.example.TMDT_Backend.validator.annotation.DifferentFromOldPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DifferentFromOldPasswordValidator implements ConstraintValidator<DifferentFromOldPassword, ChangePasswordRequest> {

    @Override
    public boolean isValid(ChangePasswordRequest dto, ConstraintValidatorContext context) {
        if (dto.getOldPassword() == null || dto.getNewPassword() == null) {
            return true; // Để NotBlank lo phần null
        }
        return !dto.getOldPassword().equals(dto.getNewPassword());
    }
}
