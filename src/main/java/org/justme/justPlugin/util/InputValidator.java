package org.justme.justPlugin.util;

import java.util.regex.Pattern;

/**
 * Utility class for validating and sanitizing user inputs.
 * Prevents YAML injection, overly long names, and invalid characters
 * in user-provided names (warps, homes, teams, etc.).
 */
public final class InputValidator {
    private static final Pattern SAFE_NAME = Pattern.compile("^[a-zA-Z0-9_\\-]{1,32}$");

    private InputValidator() {}

    /**
     * Validates that a name contains only safe characters (letters, numbers, underscores, hyphens)
     * and is between 1 and 32 characters long.
     *
     * @param input the name to validate
     * @return true if the name is valid, false otherwise
     */
    public static boolean isValidName(String input) {
        if (input == null || input.isEmpty()) return false;
        return SAFE_NAME.matcher(input).matches();
    }

    /**
     * Checks if the input contains characters that could be used for YAML injection.
     *
     * @param input the string to check
     * @return true if the input contains potentially dangerous YAML characters
     */
    public static boolean containsYamlInjection(String input) {
        return input.contains(":") || input.contains("\n") || input.contains("\r") || input.contains("\t");
    }
}
