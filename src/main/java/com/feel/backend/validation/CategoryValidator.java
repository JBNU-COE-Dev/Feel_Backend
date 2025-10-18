package com.feel.backend.validation;

import com.feel.backend.entity.NoticeCategory;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CategoryValidator implements ConstraintValidator<ValidCategory, String> {

    @Override
    public void initialize(ValidCategory constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null 값은 허용 (필수 검증은 @NotBlank로 처리)
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        // 카테고리 검증
        return NoticeCategory.isValid(value);
    }
}
