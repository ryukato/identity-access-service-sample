package app.domain;

import java.util.Arrays;

public enum EndUserStatus {
    CREATED, ACTIVE, SUSPENDED, TERMINATED, UNKNOWN;

    public static EndUserStatus fromString(String value) {
        if (value == null || value.isEmpty()) {
            return UNKNOWN;
        }
        return Arrays.stream(values())
                .filter(g -> g.name().equalsIgnoreCase(value))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
