package com.kalwidevelopment.kaprivatemessage.velocity.util;

import com.kalwidevelopment.kaprivatemessage.velocity.KAPrivateMessageVelocity;

public final class DebugLogger {
    private DebugLogger() {}

    public static void log(KAPrivateMessageVelocity plugin, String message) {
        if (plugin == null || plugin.getPluginConfig() == null || !plugin.getPluginConfig().isDebugEnabled()) return;
        plugin.getLogger().info("[DEBUG] {}", message);
    }
}
