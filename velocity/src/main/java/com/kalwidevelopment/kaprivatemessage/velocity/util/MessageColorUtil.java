package com.kalwidevelopment.kaprivatemessage.velocity.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MessageColorUtil {
    private MessageColorUtil() {}

    private static final Pattern LEGACY_HEX_COMPACT = Pattern.compile("(?i)&\\#([0-9a-f]{6})");
    private static final Pattern LEGACY_HEX_LONG = Pattern.compile("(?i)&x(?:&[0-9a-f]){6}");

    public static String applyPlayerMessageColors(String input, boolean allowColors) {
        if (input == null || input.isEmpty()) return "";
        String escaped = escapeMiniMessage(input);
        if (!allowColors) return escaped;

        String out = translateCompactHex(escaped);
        out = translateLongHex(out);
        out = translateLegacy(out);
        return out;
    }

    private static String translateCompactHex(String input) {
        Matcher matcher = LEGACY_HEX_COMPACT.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "<#" + matcher.group(1) + ">");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String translateLongHex(String input) {
        Matcher matcher = LEGACY_HEX_LONG.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String token = matcher.group();
            String hex = ""
                + token.charAt(3)
                + token.charAt(5)
                + token.charAt(7)
                + token.charAt(9)
                + token.charAt(11)
                + token.charAt(13);
            matcher.appendReplacement(sb, "<#" + hex + ">");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String translateLegacy(String input) {
        String out = input;
        out = out.replaceAll("(?i)&0", "<black>");
        out = out.replaceAll("(?i)&1", "<dark_blue>");
        out = out.replaceAll("(?i)&2", "<dark_green>");
        out = out.replaceAll("(?i)&3", "<dark_aqua>");
        out = out.replaceAll("(?i)&4", "<dark_red>");
        out = out.replaceAll("(?i)&5", "<dark_purple>");
        out = out.replaceAll("(?i)&6", "<gold>");
        out = out.replaceAll("(?i)&7", "<gray>");
        out = out.replaceAll("(?i)&8", "<dark_gray>");
        out = out.replaceAll("(?i)&9", "<blue>");
        out = out.replaceAll("(?i)&a", "<green>");
        out = out.replaceAll("(?i)&b", "<aqua>");
        out = out.replaceAll("(?i)&c", "<red>");
        out = out.replaceAll("(?i)&d", "<light_purple>");
        out = out.replaceAll("(?i)&e", "<yellow>");
        out = out.replaceAll("(?i)&f", "<white>");
        out = out.replaceAll("(?i)&k", "<obfuscated>");
        out = out.replaceAll("(?i)&l", "<bold>");
        out = out.replaceAll("(?i)&m", "<strikethrough>");
        out = out.replaceAll("(?i)&n", "<underlined>");
        out = out.replaceAll("(?i)&o", "<italic>");
        out = out.replaceAll("(?i)&r", "<reset>");
        return out;
    }

    private static String escapeMiniMessage(String input) {
        return input.replace("<", "\\<");
    }
}

