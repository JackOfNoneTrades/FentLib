package org.fentanylsolutions.fentlib.util;

public class StringUtil {

    /**
     * Trims whitespace and returns null for empty/blank strings.
     *
     * @return the trimmed string, or null if blank/null
     */
    public static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Strips trailing slashes from a URL string.
     * Returns null for blank/null input.
     */
    public static String stripTrailingSlashes(String raw) {
        String value = trimToNull(raw);
        if (value == null) return null;
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value.isEmpty() ? null : value;
    }
}
