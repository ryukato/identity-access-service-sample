package app.domain;

import java.util.Arrays;

public enum Gender {
    MALE, FEMALE, NONE;

    public static Gender fromString(String value) {
        if (value == null || value.isEmpty()) {
            return NONE;
        }
        return Arrays.stream(values())
                .filter(g -> g.name().equalsIgnoreCase(value))
                .findFirst()
                .orElse(NONE);
    }
}
