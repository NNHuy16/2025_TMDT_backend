package com.example.TMDT_Backend.validator;

import com.example.TMDT_Backend.dto.PasswordConfirmable;
import com.example.TMDT_Backend.validator.annotation.PasswordMatches;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        if (obj instanceof PasswordConfirmable dto) {
            return dto.getPassword() != null && dto.getPassword().equals(dto.getConfirmPassword());
        }
        return false;
    }
}
