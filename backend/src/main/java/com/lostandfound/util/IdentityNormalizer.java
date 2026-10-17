package com.lostandfound.util;

import java.util.Locale;

/** One place that decides what "the same email / phone" means. */
public final class IdentityNormalizer {

    private IdentityNormalizer() {}

    /** Trimmed + lower-case. Blank becomes null (users.email is nullable and unique). */
    public static String email(String raw) {
        if (raw == null || raw.isBlank()) return null;
        return raw.trim().toLowerCase(Locale.ROOT);
    }

    /** Spaces and dashes removed, so "98765 43210" and "98765-43210" are the same number. */
    public static String phone(String raw) {
        if (raw == null || raw.isBlank()) return null;
        return raw.replaceAll("[\\s-]", "");
    }

    /** At least 7 digits after cleaning. */
    public static boolean isValidPhone(String normalized) {
        return normalized != null && normalized.replaceAll("\\D", "").length() >= 7;
    }
}