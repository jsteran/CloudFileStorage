package dev.anton_kulakov.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class ResourcePathValidator implements ConstraintValidator<ValidPath, String> {
    public final static Pattern PATH_VALIDATION_PATTERN = Pattern.compile("^(|(/)?([a-zA-Z0-9_. -]+(?:/[a-zA-Z0-9_. -]+)*)(/)?)$");

    @Override
    public boolean isValid(String path, ConstraintValidatorContext constraintValidatorContext) {
        if (path == null) {
            return false;
        }

        return PATH_VALIDATION_PATTERN.matcher(path).matches();
    }
}
