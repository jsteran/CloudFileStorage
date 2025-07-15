package dev.anton_kulakov.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.text.Normalizer;

public class ResourcePathValidator implements ConstraintValidator<ValidPath, String> {
    private final static int PATH_SEGMENT_MAX_LENGTH = 50;

    @Override
    public boolean isValid(String path, ConstraintValidatorContext constraintValidatorContext) {
        if (path == null) {
            addViolation(constraintValidatorContext, "Path cannot be null");
            return false;
        }

        String normalizedPath = Normalizer.normalize(path, Normalizer.Form.NFC);

        if (normalizedPath.contains("\\")) {
            addViolation(constraintValidatorContext, "Backslashes are not allowed in path; use forward slashes (/) instead");
            return false;
        }

        if (normalizedPath.contains("//")) {
            addViolation(constraintValidatorContext, "Path can't contain consecutive slashes (//)");
            return false;
        }

        if (normalizedPath.contains("|")) {
            addViolation(constraintValidatorContext, "The character '|' is not allowed in the path");
            return false;
        }

        if (normalizedPath.isEmpty()) {
            return true;
        }

        String strippedPath = normalizedPath;

        if (strippedPath.startsWith("/")) {
            strippedPath = strippedPath.substring(1);
        }

        if (strippedPath.endsWith("/")) {
            strippedPath = strippedPath.substring(0, strippedPath.length() - 1);
        }

        if (strippedPath.isEmpty()) {
            return true;
        }

        String[] segments = strippedPath.split("/");

        for (String segment : segments) {
            if (segment.isEmpty()) {
                addViolation(constraintValidatorContext, "Path can't contain empty segments (caused by consecutive slashes like 'folder//file')");
                return false;
            }

            if (segment.length() > PATH_SEGMENT_MAX_LENGTH) {
                addViolation(constraintValidatorContext, "Path segment '%s' shouldn't be longer than %d letters".formatted(segment, PATH_SEGMENT_MAX_LENGTH));
                return false;
            }

            for (char c : segment.toCharArray()) {
                if (!isAllowedCharacter(c)) {
                    String message = "The character '%c' is not allowed. Please use only letters, numbers, spaces, periods, hyphens, underscores and parentheses".formatted(c);
                    addViolation(constraintValidatorContext, message);
                    return false;
                }
            }
        }

        return true;
    }

    private static void addViolation(ConstraintValidatorContext constraintValidatorContext, String message) {
        constraintValidatorContext.disableDefaultConstraintViolation();
        constraintValidatorContext.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }

    private boolean isAllowedCharacter(char character) {
        if (Character.isLetter(character) || Character.isDigit(character)) {
            return true;
        }

        return switch (character) {
            case '_', '.', ' ', '(', ')', '-' -> true;
            default -> false;
        };
    }
}
