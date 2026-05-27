package com.kalwidevelopment.kaprivatemessage.paper.hook;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public class CoreProtectHook {

    public boolean logCommand(Player sender, String targetName, String message) {
        Plugin coreProtect = Bukkit.getPluginManager().getPlugin("CoreProtect");
        if (coreProtect == null || !coreProtect.isEnabled()) {
            return false;
        }

        try {
            Method getApiMethod = coreProtect.getClass().getMethod("getAPI");
            Object api = getApiMethod.invoke(coreProtect);
            if (api == null) {
                return false;
            }

            Method logCommand = api.getClass().getMethod("logCommand", Player.class, String.class);
            Object result = logCommand.invoke(api, sender, "/msg " + targetName + " " + message);
            return result instanceof Boolean && (Boolean) result;
        } catch (Exception ignored) {
            // CoreProtect API tidak tersedia / berubah, abaikan tanpa mengganggu PM.
            return false;
        }
    }
}
