package dev.anton_kulakov.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ResourcePathValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPath {
    String message() default "the path is invalid";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
