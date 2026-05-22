package com.dormitory.management.util;

public final class ValidationUtil {

    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    private ValidationUtil() {
    }

    public static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public static boolean minLength(String value, int minLength) {
        return value != null && value.length() >= minLength;
    }

    public static boolean isValidEmail(String value) {
        return hasText(value) && value.matches(EMAIL_PATTERN);
    }
}
