package com.feel.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CategoryValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCategory {
    String message() default "유효하지 않은 카테고리입니다. 허용된 카테고리: 학과소식, 일반공지, 학사공지, 사업단공지, 취업정보";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
