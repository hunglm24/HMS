package util;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class ValidationUtil {
    // Utility class; do not instantiate.
    private ValidationUtil() {
    }

    // Trim text and convert null to an empty string.
    public static String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }

    // Normalize text to uppercase using a stable locale.
    public static String normalizeUpper(String value) {
        String text = normalizeText(value);
        return text.isEmpty() ? "" : text.toUpperCase(Locale.ROOT);
    }

    // Normalize text to lowercase using a stable locale.
    public static String normalizeLower(String value) {
        String text = normalizeText(value);
        return text.isEmpty() ? "" : text.toLowerCase(Locale.ROOT);
    }

    // Check whether a string is null, empty, or blank.
    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    // Require a non-empty text value and enforce length bounds.
    public static String requireText(String value, String fieldName, int minLength, int maxLength) {
        String text = normalizeText(value);
        if (text.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " bat buoc.");
        }
        if (text.length() < minLength || text.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " phai co do dai tu " + minLength
                    + " den " + maxLength + " ky tu.");
        }
        return text;
    }

    // Require a non-empty text value that also matches an allowed regex.
    public static String requirePatternText(
            String value,
            String fieldName,
            int minLength,
            int maxLength,
            Pattern pattern,
            String invalidMessage
    ) {
        String text = requireText(value, fieldName, minLength, maxLength);
        if (pattern != null && !pattern.matcher(text).matches()) {
            throw new IllegalArgumentException(invalidMessage);
        }
        return text;
    }

    // Require a non-empty text value that contains only digits.
    public static String requireDigitsText(String value, String fieldName, int minLength, int maxLength) {
        String text = requireText(value, fieldName, minLength, maxLength);
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isDigit(text.charAt(i))) {
                throw new IllegalArgumentException(fieldName + " phai la so nguyen hop le.");
            }
        }
        return text;
    }

    // Allow empty text, but enforce a maximum length when provided.
    public static String optionalText(String value, int maxLength) {
        String text = normalizeText(value);
        if (text.length() > maxLength) {
            throw new IllegalArgumentException("Gia tri khong duoc vuot qua " + maxLength + " ky tu.");
        }
        return text;
    }

    // Require a positive integer value from text input.
    public static int requirePositiveInt(String value, String fieldName) {
        if (isBlank(value)) {
            throw new IllegalArgumentException(fieldName + " bat buoc.");
        }
        try {
            int result = Integer.parseInt(value.trim());
            if (result < 1) {
                throw new IllegalArgumentException(fieldName + " phai lon hon hoac bang 1.");
            }
            return result;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " phai la so nguyen hop le.");
        }
    }

    // Allow an integer value to be absent, but reject negative numbers.
    public static Integer optionalPositiveInt(String value, String fieldName) {
        if (isBlank(value)) {
            return null;
        }
        try {
            int result = Integer.parseInt(value.trim());
            if (result < 0) {
                throw new IllegalArgumentException(fieldName + " khong duoc nho hon 0.");
            }
            return result;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " phai la so nguyen hop le.");
        }
    }

    // Require a positive long value from text input.
    public static long requirePositiveLong(String value, String fieldName) {
        if (isBlank(value)) {
            throw new IllegalArgumentException(fieldName + " bat buoc.");
        }
        try {
            long result = Long.parseLong(value.trim());
            if (result < 1L) {
                throw new IllegalArgumentException(fieldName + " phai lon hon hoac bang 1.");
            }
            return result;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " phai la so hop le.");
        }
    }

    // Allow a long value to be absent, but reject negative numbers.
    public static Long optionalPositiveLong(String value, String fieldName) {
        if (isBlank(value)) {
            return null;
        }
        try {
            long result = Long.parseLong(value.trim());
            if (result < 0L) {
                throw new IllegalArgumentException(fieldName + " khong duoc nho hon 0.");
            }
            return result;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " phai la so hop le.");
        }
    }

    // Require a positive decimal value from text input.
    public static BigDecimal requirePositiveBigDecimal(String value, String fieldName) {
        if (isBlank(value)) {
            throw new IllegalArgumentException(fieldName + " bat buoc.");
        }
        String raw = value.trim();
        try {
            BigDecimal result = new BigDecimal(raw);
            if (result.signum() <= 0) {
                throw new IllegalArgumentException(fieldName + " phai lon hon 0.");
            }
            return result;
        } catch (NumberFormatException ex) {
            String compact = raw.replaceAll("[,\\s._']", "");
            if (!compact.isEmpty() && !compact.equals(raw)) {
                try {
                    BigDecimal result = new BigDecimal(compact);
                    if (result.signum() <= 0) {
                        throw new IllegalArgumentException(fieldName + " phai lon hon 0.");
                    }
                    return result;
                } catch (NumberFormatException ignored) {
                    // Fall through to the standard error message below.
                }
            }
            throw new IllegalArgumentException(fieldName + " phai la so hop le.");
        }
    }

    // Allow a decimal value to be absent.
    public static BigDecimal optionalBigDecimal(String value, String fieldName) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " phai la so hop le.");
        }
    }

    // Require a status value from a predefined allowed set.
    public static String requireStatus(String value, String fieldName, Set<String> allowedStatuses) {
        String status = normalizeUpper(value);
        if (status.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " bat buoc.");
        }
        if (allowedStatuses == null || allowedStatuses.isEmpty() || !allowedStatuses.contains(status)) {
            throw new IllegalArgumentException(fieldName + " khong hop le.");
        }
        return status;
    }

    // Allow an optional status value, but validate it when present.
    public static String optionalStatus(String value, Set<String> allowedStatuses) {
        String status = normalizeUpper(value);
        if (status.isEmpty()) {
            return null;
        }
        if (allowedStatuses == null || allowedStatuses.isEmpty() || !allowedStatuses.contains(status)) {
            throw new IllegalArgumentException("Trang thai khong hop le.");
        }
        return status;
    }

    // Throw an IllegalArgumentException when a condition is false.
    public static void requireTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
