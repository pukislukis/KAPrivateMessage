package com.kalwidevelopment.kaprivatemessage.common;

public enum PrivacyLevel {
    NONE,
    LOW,
    MEDIUM,
    HIGH;

    public static PrivacyLevel fromString(String s) {
        if (s == null) return NONE;
        switch (s.toUpperCase()) {
            case "HIGH": return HIGH;
            case "MEDIUM": return MEDIUM;
            case "LOW": return LOW;
            default: return NONE;
        }
    }
}
