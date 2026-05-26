package com.kalwidevelopment.kaprivatemessage.paper.hook;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public class CoreProtectHook {

    public void logCommand(String senderName, String targetName, String message) {
        Plugin coreProtect = Bukkit.getPluginManager().getPlugin("CoreProtect");
        if (coreProtect == null || !coreProtect.isEnabled()) {
            return;
        }

        try {
            Method getApiMethod = coreProtect.getClass().getMethod("getAPI");
            Object api = getApiMethod.invoke(coreProtect);
            if (api == null) {
                return;
            }

            Method logChat = api.getClass().getMethod("logChat", String.class, String.class);
            logChat.invoke(api, senderName, "/msg " + targetName + " " + message);
        } catch (Exception ignored) {
            // CoreProtect API tidak tersedia / berubah, abaikan tanpa mengganggu PM.
        }
    }
}
