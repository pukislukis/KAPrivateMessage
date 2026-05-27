package com.kalwidevelopment.kaprivatemessage.paper.hook;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class LuckPermsHook {

    private final JavaPlugin plugin;
    private LuckPerms luckPerms;
    private boolean enabled = false;

    public LuckPermsHook(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        try {
            this.luckPerms = LuckPermsProvider.get();
            this.enabled = true;
            plugin.getLogger().info("LuckPerms hook enabled.");
        } catch (Exception e) {
            plugin.getLogger().info("LuckPerms not found. Prefix fallback disabled.");
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getPrefix(Player player) {
        if (!enabled || luckPerms == null || player == null) return "";
        try {
            User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);
            if (user == null) return "";
            String prefix = user.getCachedData().getMetaData().getPrefix();
            return prefix == null ? "" : prefix;
        } catch (Exception e) {
            return "";
        }
    }
}
